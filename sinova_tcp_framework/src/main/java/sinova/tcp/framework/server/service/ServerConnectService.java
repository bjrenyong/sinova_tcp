package sinova.tcp.framework.server.service;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.common.FrameworkConstants;
import sinova.tcp.framework.common.TcpErrorCode;
import sinova.tcp.framework.common.exception.BusinessException;
import sinova.tcp.framework.common.monitor.IOverWaitSpeed;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.common.service.TransportMsgService;
import sinova.tcp.framework.common.window.SyncWindowMsg;
import sinova.tcp.framework.common.window.WindowMsg;
import sinova.tcp.framework.server.ServerConstants;
import sinova.tcp.framework.server.entity.ServerUserBase;
import sinova.tcp.protocol.ILoginReq;
import sinova.tcp.protocol.ILoginResp;
import sinova.tcp.protocol.IMsg;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;
import sinova.tcp.protocol.simple.ErrorResp;
import sinova.tcp.protocol.simple.LoginReq;
import sinova.tcp.protocol.simple.LoginResp;

/**
 * 服务端客户连接请求管理实现类<br/>
 * 一个用户只能有一个netty连接，这个连接可以是双工、仅下行、仅上行
 * @author Timothy
 */
@Service
public class ServerConnectService implements IServerConnectService<ServerUserBase> {

	private static final Logger logger = LoggerFactory.getLogger(ServerConnectService.class);

	/** 服务端用户信息服务，依赖注入 */
	@Autowired
	private IServerUserService serverUserService;
	/** 传输消息服务，依赖注入 */
	@Autowired
	private TransportMsgService transportMsgService;
	/** 服务端限速服务，依赖注入 */
	@Autowired
	private IServerSpeedService serverSpeedService;

	/** 下行用连接信息MAP, key-用户ID；value-Channel */
	private Map<Integer, Channel> inBoundConnectMap = new ConcurrentHashMap<Integer, Channel>();
	/** 上行用连接信息MAP, key-用户ID；value-Channel */
	private Map<Integer, Channel> outBoundConnectMap = new ConcurrentHashMap<Integer, Channel>();
	/** Channel到用户信息MAP */
	private Map<Channel, ServerUserBase> channel2UserMap = new ConcurrentHashMap<Channel, ServerUserBase>();

	/**
	 * 执行登录操作<br/>
	 * 需要与相应的LoginHandler一起配合实现登录操作
	 * @param loginReq 登录请求消息
	 * @param channel netty channel
	 * @return 登录响应
	 */
	public ILoginResp login(ILoginReq loginReq, Channel channel) {
		LoginReq req = (LoginReq) loginReq;
		// 进行登录的用户名和密码验证
		ServerUserBase serverUser = serverUserService.getServerUserByUserId(req.getUserId());
		if (!serverUserService.validateAuth(serverUser, req)) {
			int connectionType = serverUser == null ? -1 : serverUser.getConnectionType();
			// 验证失败，返回失败的登录响应消息
			return createLoginResp(false, ServerConstants.LOGIN_CODE_AUTH_FAIL, connectionType);
		}

		if (serverUser.getConnectionType() == ServerConstants.USER_CONNECTIONTYPE_ALL) {
			// 用户连接类型为双工
			if (check4ActiveConnection(serverUser.getUserId(), inBoundConnectMap)
					|| check4ActiveConnection(serverUser.getUserId(), outBoundConnectMap)) {
				// 已经存在有效的连接了，是重复连接
				return createLoginResp(false, ServerConstants.LOGIN_CODE_DUPLICATE, serverUser.getConnectionType());
			} else {
				// 登录成功，将连接保存
				inBoundConnectMap.put(serverUser.getUserId(), channel);
				outBoundConnectMap.put(serverUser.getUserId(), channel);
				channel2UserMap.put(channel, serverUser);
				// 返回连接成功的登录响应
				return createLoginResp(true, ServerConstants.LOGIN_CODE_SUCCESS, serverUser.getConnectionType());
			}
		} else if (serverUser.getConnectionType() == ServerConstants.USER_CONNECTIONTYPE_MT) {
			// 用户连接类型为仅下行
			if (check4ActiveConnection(serverUser.getUserId(), inBoundConnectMap)) {
				// 已经存在有效的连接，是重复连接
				return createLoginResp(false, ServerConstants.LOGIN_CODE_DUPLICATE, serverUser.getConnectionType());
			} else {
				// 登录成功，将连接保存
				inBoundConnectMap.put(serverUser.getUserId(), channel);
				channel2UserMap.put(channel, serverUser);
				// 返回连接成功的登录响应
				return createLoginResp(true, ServerConstants.LOGIN_CODE_SUCCESS, serverUser.getConnectionType());
			}
		} else if (serverUser.getConnectionType() == ServerConstants.USER_CONNECTIONTYPE_MO) {
			// 用户连接类型为仅上行
			if (check4ActiveConnection(serverUser.getUserId(), outBoundConnectMap)) {
				// 已经存在有效的连接，是重复连接
				return createLoginResp(false, ServerConstants.LOGIN_CODE_DUPLICATE, serverUser.getConnectionType());
			} else {
				// 登录成功，将连接保存
				outBoundConnectMap.put(serverUser.getUserId(), channel);
				channel2UserMap.put(channel, serverUser);
				// 返回连接成功的登录响应
				return createLoginResp(true, ServerConstants.LOGIN_CODE_SUCCESS, serverUser.getConnectionType());
			}
		} else {
			// 用户连接类型异常
			return createLoginResp(false, ServerConstants.LOGIN_CODE_SERVEREXCEPTION, serverUser.getConnectionType());
		}
	}

	/**
	 * 检查特定的用户是否已经存在有效的连接<br>
	 * 当前仅允许一个用户建立一个有效的连接
	 * @param userId 用户ID
	 * @param connectMap 有效连接MAP
	 * @return 用户已存在有效连接返回true，否则返回false
	 */
	private boolean check4ActiveConnection(Integer userId, Map<Integer, Channel> connectMap) {
		Channel connect = connectMap.get(userId);
		if (connect == null) {
			return false;
		} else if (!connect.isActive()) {
			connect.close();
			connectMap.remove(userId);
			channel2UserMap.remove(connect);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 响应登出请求
	 * @param channel netty channel
	 */
	@Override
	public void logout(Channel channel) {
		ServerUserBase serverUser = channel2UserMap.get(channel);
		if (serverUser != null) {
			inBoundConnectMap.remove(serverUser.getUserId());
			outBoundConnectMap.remove(serverUser.getUserId());
			channel2UserMap.remove(channel);
		}
	}

	/**
	 * 获取某个连接通道所归属的用户
	 * @param channel netty channel
	 * @return 此连接通道归属的用户
	 */
	@Override
	public ServerUserBase getUserByChannel(Channel channel) {
		return this.channel2UserMap.get(channel);
	}

	@Override
	public Channel getChannelByUserId(Integer userId) {
		if (inBoundConnectMap.containsKey(userId)) {
			return inBoundConnectMap.get(userId);
		} else if (outBoundConnectMap.containsKey(userId)) {
			return outBoundConnectMap.get(userId);
		} else {
			return null;
		}
	}

	/**
	 * 创建登录响应信息
	 * @param success 是否登录成功
	 * @param errorCode 登录错误码
	 * @return 登录响应信息
	 */
	private LoginResp createLoginResp(boolean success, String errorCode, int connectionType) {
		LoginResp loginResp = new LoginResp();
		loginResp.setSuccess(success);
		loginResp.setErrorCode(errorCode);
		loginResp.setConnectionType(connectionType);
		return loginResp;
	}

	/**
	 * 获取匹配传输响应消息的窗口消息
	 * @param transportRespMsg 传输响应消息
	 * @param channel 指定的连接通道
	 * @return 匹配传输响应消息的窗口消息(匹配不到返回null)
	 */
	@Override
	public WindowMsg responseMatchWindowMsg(TransportMsg transportRespMsg, Channel channel) {
		ServerUserBase serverUser = channel2UserMap.get(channel);
		if (serverUser == null) {
			// 这个警告不应该出现，若出现需要排查原因
			logger.warn("the channel has no server user, why? channel=" + channel);
			return null;
		}
		int sequenceId = transportRespMsg.getSequenceId();
		WindowMsg windowMsg = serverUser.getSlidingWindow().removeMsg(sequenceId);
		if (windowMsg == null) {
			// 传输响应消息找不到对应的滑动窗口消息
			logger.warn("the response message can't match any window message, maybe timeout! transportRespMsg="
					+ transportRespMsg);
			return null;
		} else {
			// 发送成功计数
			serverUser.getServerSpeedRecord().addSendSuccessCount(1);
			if (windowMsg instanceof SyncWindowMsg) {
				// 同步请求类的窗口消息，需要向窗口消息回写传输响应消息
				((SyncWindowMsg) windowMsg).setTransportRespMsg(transportRespMsg);
			}
			return windowMsg;
		}
	}

	/**
	 * 判断用户的连接类型是否允许服务端的业务发送请求
	 * @return boolean
	 */
	private boolean isAllowReqSend(ServerUserBase serverUser) {
		// 当用户的连接类型为全双工或仅上行时，返回true,否则返回false
		if (serverUser.getConnectionType() == FrameworkConstants.CONNECTION_TYPE_ALL
				|| serverUser.getConnectionType() == FrameworkConstants.CONNECTION_TYPE_MO) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 将请求传输消息封装成窗口消息
	 * @param transportReqMsg 传输请求消息
	 * @param sync 是否为同步调用
	 * @return 封装的窗口消息
	 */
	private WindowMsg package2WindowMsg(TransportMsg transportReqMsg, boolean sync) {
		if (!sync) {
			return new WindowMsg(transportReqMsg);
		} else {
			return new SyncWindowMsg(transportReqMsg);
		}
	}

	/**
	 * 执行业务请求发送<br/>
	 * 对于可以重发的错误，抛出可重发异常ResendableException<br/>
	 * 对于不可以重发的错误，抛出普通异常Exception<br/>
	 * @param req 业务请求消息
	 * @param channel 发送使用的连接通道
	 * @param sync 是否为同步调用
	 * @return 业务请求对应的窗口消息
	 * @throws Exception
	 */
	private WindowMsg businessReqSend(IReq req, Channel channel, boolean sync) throws BusinessException {
		// 获取该通道对应的用户信息
		ServerUserBase serverUser = this.channel2UserMap.get(channel);
		if (serverUser == null) {
			logger.warn("obtain the serverUser failed, can't send and discard! req=" + req);
			// 通道连接异常，可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_TCP_CONNECTION);
		}
		// 判断用户的连接类型是否允许服务端的业务发送请求
		if (!isAllowReqSend(serverUser)) {
			// 连接类型决定了该用户的服务端不被允许业务请求发送
			logger.warn("the connect type not allow send business request, discard the message! userId="
					+ serverUser.getUserId());
			// 没有发出去的也算发送失败
			serverUser.getServerSpeedRecord().addSendErrorCount(1);
			// 连接类型不允许发送消息到客户端，不可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_NOTALLOW_MO);
		}
		// 请求消息包装成请求传输消息
		TransportMsg transportMsg = transportMsgService.req2TransportMsg(req);
		if (transportMsg == null) {
			logger.warn("req to transportMsg failed, can't send and discard! req=" + req);
			// 没有发出去的也算发送失败，发送失败计数
			serverUser.getServerSpeedRecord().addSendErrorCount(1);
			// 消息格式错误，不可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_MESSAGE_FORMAT);
		}

		try {
			// 请求传输消息包装成窗口消息
			WindowMsg windowMsg = package2WindowMsg(transportMsg, sync);
			// 判断是否需要进行发送限速？（是否设置了该用户的发送限速器）
			IOverWaitSpeed sendSpeed = serverSpeedService.getSendSpeedByUserId(serverUser.getUserId());
			if (sendSpeed != null) {
				// 执行发送计数限制
				sendSpeed.limitSpeed();
			}
			logger.info("send a message to client! userId={}, mtReq={}", serverUser.getUserId(), req);
			// 窗口消息放入该client对应的滑动窗口
			serverUser.getSlidingWindow().putMsg(transportMsg.getSequenceId(), windowMsg);
			channel.writeAndFlush(transportMsg);
			// 有效发送数+1
			serverUser.getServerSpeedRecord().addValidSendTotal(1);
			return windowMsg;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			serverUser.getSlidingWindow().removeMsg(transportMsg.getSequenceId());
			// 发送失败计数
			serverUser.getServerSpeedRecord().addSendErrorCount(1);
			// 系统异常，可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_SYSTEM_EXCEPTION);
		}
	}

	/**
	 * 异步业务请求消息发送<br/>
	 * 对于可以重发的错误，抛出可重发异常ResendableException<br/>
	 * 对于不可以重发的错误，抛出普通异常Exception<br/>
	 * @param req 业务请求消息
	 * @throws BusinessException
	 */
	public void businessReqSendAsync(IReq req, Channel channel) throws BusinessException {
		businessReqSend(req, channel, false);
	}

	/**
	 * 业务请求消息发送(同步)
	 * @param req 业务请求消息
	 * @throws BusinessException
	 */
	public IResp businessReqSendSync(IReq req, Channel channel) throws BusinessException {
		// 同步业务请求消息发送
		SyncWindowMsg syncWindowMsg = (SyncWindowMsg) this.businessReqSend(req, channel, true);
		TransportMsg transportRespMsg = syncWindowMsg.get();
		if (transportRespMsg == null) {
			// 发送超时，可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_TIMEOUT);
		}
		IMsg respMsg = transportRespMsg.getMsg();
		if (respMsg == null) {
			// 发送超时，可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_TIMEOUT);
		} else if (respMsg instanceof ErrorResp) {
			// 发送处理失败，是否可重发看具体的错误码
			throw new BusinessException((ErrorResp) respMsg);
		} else if (respMsg instanceof IResp) {
			logger.info("sync send a message success! respMsg=" + respMsg);
			return (IResp) respMsg;
		} else {
			logger.error("response type error! respMsg=" + respMsg);
			// 消息格式错误，不可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_MESSAGE_FORMAT);
		}
	}

	public Map<Integer, Channel> getOutBoundConnectMap() {
		return outBoundConnectMap;
	}

	public Map<Channel, ServerUserBase> getChannel2UserMap() {
		return channel2UserMap;
	}
}
