package sinova.tcp.framework.common.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.action.IReqAction;
import sinova.tcp.framework.action.IRespAction;
import sinova.tcp.framework.common.TcpErrorCode;
import sinova.tcp.framework.common.exception.BusinessException;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.common.service.ICommandReferenceService;
import sinova.tcp.framework.common.window.SyncWindowMsg;
import sinova.tcp.framework.common.window.WindowMsg;
import sinova.tcp.protocol.IMsg;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;
import sinova.tcp.protocol.simple.ErrorResp;

/**
 * 业务请求和业务响应的接收处理器抽象类<br>
 * 因为这个接收处理器接收所有的业务请求，因此在这里做接收请求计数
 * @author Timothy
 */
@Service
public abstract class AbsMsgProcessHandler extends SimpleChannelInboundHandler<TransportMsg> {

	private static final Logger logger = LoggerFactory.getLogger(AbsMsgProcessHandler.class);

	/** 命令关联信息服务，依赖注入 */
	@Autowired
	private ICommandReferenceService commandReferenceService;

	/**
	 * 处理接收到传输消息<br/>
	 * 整个处理过程中抛出的异常分为两类：ActionProcessException，其它普通异常<br/>
	 * ActionProcessException：不断开连接；其它普通异常：断开连接<br/>
	 * 1. 传输消息中的消息体应该只能是请求消息或者响应消息，否则应抛出普通异常<br/>
	 * 2. 对于请求消息：<br/>
	 * 2.1 如果用户的连接类型不允许接收业务请求，抛出普通异常<br/>
	 * 2.2 如果用户的业务请求接收超限速，封装成ErrorResp返回给请求者<br/>
	 * 2.3 没有对应的请求处理action，抛出普通异常<br/>
	 * 2.4 执行完请求处理ation后返回的response为空，抛出普通异常<br/>
	 * 2.5 对于ActionProcessException，封装成ErrorResp返回给请求者<br/>
	 * 2.6 正常的Response，返回给请求者<br/>
	 * 3. 对于响应消息：<br/>
	 * 3.1 没有对应的响应处理action，抛出普通异常<br/>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, TransportMsg transportMsg) throws Exception {
		// 获取请求消息对应的用户ID
		int userId = getUserId(ctx.channel());
		IMsg msg = transportMsg.getMsg();
		if (msg instanceof IReq) {
			// 接收用户请求计数+1
			addReceiveCount(userId, 1);
			// 如果用户的连接类型不允许接收业务请求，抛出异常并断开连接
			if (!isAllowReceiveReq(userId, (IReq) msg)) {
				// 连接类型不允许接收来自对方的业务请求
				String errorMsg = "the connect type not allow receive business request! userId=" + userId;
				logger.error(errorMsg);
				throw new Exception(errorMsg);
			}
			// 判断业务请求接收是否超限速
			if (!isAllowReceiveReqSpeed(userId)) {
				// 构造业务请求接收超限速错误并返回
				logger.warn("the business request overfow! userId=" + userId);
				ErrorResp resp = createErrorResp(TcpErrorCode.ERRORCODE_SPEED_OVERFLOW);
				TransportMsg respMsg = createResponseMsg(resp, transportMsg);
				ctx.writeAndFlush(respMsg);
				return;
			}

			IResp resp = null;
			try {
				// 收到的消息是请求消息
				IReq req = (IReq) msg;
				IReqAction reqAction = commandReferenceService.getReqActionByReqClass(req.getClass());
				if (reqAction == null) {
					// 无法处理的请求类消息
					String errorMsg = "the request message can't be processed! msgType=" + req.getClass();
					logger.error(errorMsg);
					throw new Exception(errorMsg);
				}
				// 处理请求消息得到响应消息ID
				resp = reqAction.action(req, userId);
				if (resp == null) {
					// 在处理请求的时候出现异常（业务处理必须有响应消息）
					String errorMsg = "the request message can be processed, but return null! reqMsg=" + req;
					logger.error(errorMsg);
					throw new Exception(errorMsg);
				}
			} catch (BusinessException ex) {
				// 仅将ActionProcessException转换成ErrorResp，返回给请求者，其它普通异常不捕捉，系统专门捕捉并断开netty连接
				logger.error(ex.getMessage(), ex);
				resp = new ErrorResp(ex.getErrorCode(), ex.getErrorMsg());
			}
			TransportMsg respMsg = createResponseMsg(resp, transportMsg);
			if (respMsg == null) {
				logger.error("the response message can't get commandId, resp={}", resp);
				throw new Exception("the response message can't get commandId, resp=" + resp);
			} else {
				// 发送响应消息给发起者
				ctx.writeAndFlush(respMsg);
				// Storage.addServeRespCount(userId);
				// Storage.addServerRespTime(transportMsg.getSequenceId(), userId, System.currentTimeMillis());
			}
		} else if (msg instanceof IResp) {
			// 收到的消息是响应消息
			IResp resp = (IResp) msg;
			// 匹配获取对应的窗口消息
			WindowMsg windowMsg = responseMatchWindowMsg(transportMsg, ctx.channel());
			if (windowMsg == null) {
				IRespAction respAction = commandReferenceService.getRespActionByRespClass(resp.getClass());
				if (respAction == null) {
					logger.error("the response message can't be processed! resp={}" + resp);
					return;
				} else {
					respAction.action(null, resp);
					return;
				}
			} else if (windowMsg instanceof SyncWindowMsg) {
				// 窗口消息是同步消息，不再做任何操作
				return;
			} else {
				// 窗口消息是异步消息
				TransportMsg transportReqMsg = windowMsg.getTransportReqMsg();
				IReq req = (transportReqMsg == null) ? null : (IReq) transportReqMsg.getMsg();
				IRespAction respAction = commandReferenceService.getRespActionByRespClass(resp.getClass());
				if (respAction == null) {
					// 找不到对应的响应处理action，抛出普通异常（即便是IErrorResp，也应该有相应的处理action）
					throw new Exception("the response message can't be processed! msgType=" + resp.getClass());
				} else {
					respAction.action(req, resp);
				}
			}
		} else {
			logger.error("the message can't be processed! msgType={}", transportMsg.getClass());
			throw new Exception("the message can't be processed! msgType=" + transportMsg.getClass());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("error cause connection close! channel=" + ctx.channel());
		logger.error(cause.getMessage(), cause);
		ctx.close();
	}

	private ErrorResp createErrorResp(TcpErrorCode tcpErrorCode) {
		ErrorResp resp = new ErrorResp(tcpErrorCode.getErrorCode(), tcpErrorCode.getErrorMsg());
		return resp;
	}

	/**
	 * 创建传输响应消息
	 * @param resp 响应消息
	 * @param transportReqMsg 传输请求消息
	 * @return 传输响应消息
	 */
	private TransportMsg createResponseMsg(IResp resp, TransportMsg transportReqMsg) {
		Short commandId = commandReferenceService.getRespCommandIdByRespClass(resp.getClass());
		if (commandId == null) {
			return null;
		} else {
			TransportMsg respMsg = new TransportMsg();
			respMsg.setCommandId(commandId);
			respMsg.setSequenceId(transportReqMsg.getSequenceId());
			respMsg.setMsg(resp);
			return respMsg;
		}
	}

	/**
	 * 由传输响应消息匹配对应的滑动窗口消息
	 * @param transportRespMsg 传输响应消息
	 * @param channel 连接通道
	 * @return 匹配的滑动窗口消息
	 */
	protected abstract WindowMsg responseMatchWindowMsg(TransportMsg transportRespMsg, Channel channel);

	/**
	 * 获取连接通道所归属的用户ID
	 * @param channel 连接通道
	 * @return 用户ID
	 */
	protected abstract int getUserId(Channel channel);

	/**
	 * 接收请求计数
	 * @param userId 用户ID
	 * @param count 计数值
	 */
	protected abstract void addReceiveCount(int userId, int count);

	/**
	 * 判断是否可以接收业务请求<br>
	 * 实现类既可以根据自己的需要去实现具体的逻辑：<br>
	 * 示例1：无条件可以，直接返回true<br>
	 * 示例2：判断某用户是否允许接收请求<br>
	 * 示例3：判断某用户是否允许接收某类请求消息<br>
	 * @param userId 用户ID
	 * @param req 请求消息
	 * @return boolean
	 */
	protected abstract boolean isAllowReceiveReq(int userId, IReq req);

	/**
	 * 判断是否该用户的业务请求速度合法（合法返回true，否则返回false）
	 * @param userId 用户ID
	 * @return boolean
	 */
	protected abstract boolean isAllowReceiveReqSpeed(int userId);
}
