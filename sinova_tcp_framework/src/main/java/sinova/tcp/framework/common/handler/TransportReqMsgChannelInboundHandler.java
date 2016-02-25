package sinova.tcp.framework.common.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.common.service.ICommandReferenceService;
import sinova.tcp.protocol.IMsg;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 对传输请求信息的输入处理<br/>
 * 对SimpleChannelInboundHandler进行了小小的改造，重写了其中的channelRead方法<br/>
 * 适用于几乎所有的业务请求的输入处理：获取到netty另一侧发来的业务请求，经处理得到业务响应返回给netty另一侧<br/>
 * 但是每个业务请求都做自己的实现没有必要，只要没有特殊需求，应该使用MsgProcessHandler
 * @param <I> 请求消息类型
 * @author Timothy
 */
public abstract class TransportReqMsgChannelInboundHandler<I extends IReq> extends ChannelHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(TransportReqMsgChannelInboundHandler.class);

	@Autowired
	private ICommandReferenceService commandReferenceService;

	private final TypeParameterMatcher matcher;
	private final boolean autoRelease;

	/**
	 * @see {@link #TransportMsgChannelInboundHandler(boolean)} with {@code true} as boolean parameter.
	 */
	protected TransportReqMsgChannelInboundHandler() {
		this(true);
	}

	/**
	 * Create a new instance which will try to detect the types to match out of the type parameter of the class.
	 * @param autoRelease {@code true} if handled messages should be released automatically by pass them to
	 *            {@link ReferenceCountUtil#release(Object)}.
	 */
	protected TransportReqMsgChannelInboundHandler(boolean autoRelease) {
		matcher = TypeParameterMatcher.find(this, TransportReqMsgChannelInboundHandler.class, "I");
		this.autoRelease = autoRelease;
	}

	/**
	 * @see {@link #TransportMsgChannelInboundHandler(Class, boolean)} with {@code true} as boolean value.
	 */
	protected TransportReqMsgChannelInboundHandler(Class<? extends I> messageType) {
		this(messageType, true);
	}

	/**
	 * Create a new instance
	 * @param messageType The type of messages to match
	 * @param autoRelease {@code true} if handled messages should be released automatically by pass them to
	 *            {@link ReferenceCountUtil#release(Object)}.
	 */
	protected TransportReqMsgChannelInboundHandler(Class<? extends I> messageType, boolean autoRelease) {
		matcher = TypeParameterMatcher.get(messageType);
		this.autoRelease = autoRelease;
	}

	/**
	 * Returns {@code true} if the given message should be handled. If {@code false} it will be passed to the next
	 * {@link ChannelHandler} in the {@link ChannelPipeline}.
	 */
	public boolean acceptInboundMessage(IMsg imsg) throws Exception {
		return matcher.match(imsg);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object inboundMsg) throws Exception {
		boolean release = true;
		try {
			if (!(inboundMsg instanceof TransportMsg)) {
				// 不是传输消息，不处理
				release = false;
				ctx.fireChannelRead(inboundMsg);
			} else {
				// 是传输消息
				TransportMsg transportMsg = (TransportMsg) inboundMsg;
				IMsg imsg = transportMsg.getMsg();
				// 判断其中的消息是否符合匹配要求
				if (acceptInboundMessage(imsg)) {
					@SuppressWarnings("unchecked")
					I reqMsg = (I) imsg;
					// 处理请求消息得到对应的响应消息
					IResp respMsg = processReqReturnResp(ctx, transportMsg, reqMsg);
					// 返回响应消息给请求者
					sendResp(ctx, transportMsg, respMsg);
				} else {
					// 不是本处理器能处理的消息
					release = false;
					ctx.fireChannelRead(inboundMsg);
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			ctx.fireChannelRead(inboundMsg);
		} finally {
			if (autoRelease && release) {
				ReferenceCountUtil.release(inboundMsg);
			}
		}
	}

	/**
	 * 将响应消息发送回去
	 * @param ctx ChannelHandlerContext
	 * @param transportReqMsg 传输请求消息
	 * @param respMsg 响应消息
	 * @throws Exception
	 */
	private void sendResp(ChannelHandlerContext ctx, TransportMsg transportReqMsg, IResp respMsg) throws Exception {
		if (respMsg == null) {
			// 响应消息为空的不做回送
			logger.warn("respMsg is null, so won't response, reqCommandId=" + transportReqMsg.getCommandId()
					+ ", sequenceId=" + transportReqMsg.getSequenceId());
		} else {
			Short commandId = commandReferenceService.getCommandIdByMsgClass(respMsg.getClass());
			if (commandId == null) {
				// 无法获取对应的commandId
				logger.warn("response's commandId not exist! msgType=" + respMsg.getClass());
				throw new Exception("response's commandId not exist! msgType=" + respMsg.getClass());
			}
			// 构造协议响应消息并回写给请求方
			TransportMsg transportRespMsg = new TransportMsg();
			transportRespMsg.setCommandId(commandId);
			transportRespMsg.setSequenceId(transportReqMsg.getSequenceId());
			transportRespMsg.setMsg(respMsg);
			ctx.writeAndFlush(transportRespMsg);
			if (logger.isDebugEnabled()) {
				logger.debug("send response success! transportRespMsg=" + transportRespMsg.toString());
			}
		}
		// 发送响应信息后的处理
		doAfterSendResp(ctx, transportReqMsg, respMsg);
	}

	/**
	 * 处理请求消息返回响应消息
	 * @param ctx ChannelHandlerContext
	 * @param transportReqMsg 传输请求消息
	 * @param reqMsg 请求消息
	 * @return 响应消息
	 */
	protected abstract IResp processReqReturnResp(ChannelHandlerContext ctx, TransportMsg transportReqMsg, I reqMsg);

	/**
	 * 发送响应信息后的处理
	 * @param ctx ChannelHandlerContext
	 * @param transportReqMsg 传输请求消息
	 * @param transportRespMsg 传输响应消息
	 */
	protected abstract void doAfterSendResp(ChannelHandlerContext ctx, TransportMsg transportReqMsg, IResp respMsg);

}
