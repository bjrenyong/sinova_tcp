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
import sinova.tcp.protocol.IResp;

/**
 * 对传输响应信息的输入处理<br/>
 * 对SimpleChannelInboundHandler进行了小小的改造，重写了其中的channelRead方法<br/>
 * 适用于几乎所有的业务响应的输入处理：获取到netty另一侧发来的业务响应，做针对性的处理<br/>
 * 但是每个业务请求都做自己的实现没有必要，只要没有特殊需求，应该使用MsgProcessHandler
 * @param <I> 响应消息类型
 * @author Timothy
 */
public abstract class TransportRespMsgChannelInboundHandler<I extends IResp> extends ChannelHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(TransportRespMsgChannelInboundHandler.class);

	@Autowired
	private ICommandReferenceService commandReferenceService;

	private final TypeParameterMatcher matcher;
	private final boolean autoRelease;

	/**
	 * @see {@link #TransportMsgChannelInboundHandler(boolean)} with {@code true} as boolean parameter.
	 */
	protected TransportRespMsgChannelInboundHandler() {
		this(true);
	}

	/**
	 * Create a new instance which will try to detect the types to match out of the type parameter of the class.
	 * @param autoRelease {@code true} if handled messages should be released automatically by pass them to
	 *            {@link ReferenceCountUtil#release(Object)}.
	 */
	protected TransportRespMsgChannelInboundHandler(boolean autoRelease) {
		matcher = TypeParameterMatcher.find(this, TransportRespMsgChannelInboundHandler.class, "I");
		this.autoRelease = autoRelease;
	}

	/**
	 * @see {@link #TransportMsgChannelInboundHandler(Class, boolean)} with {@code true} as boolean value.
	 */
	protected TransportRespMsgChannelInboundHandler(Class<? extends I> messageType) {
		this(messageType, true);
	}

	/**
	 * Create a new instance
	 * @param messageType The type of messages to match
	 * @param autoRelease {@code true} if handled messages should be released automatically by pass them to
	 *            {@link ReferenceCountUtil#release(Object)}.
	 */
	protected TransportRespMsgChannelInboundHandler(Class<? extends I> messageType, boolean autoRelease) {
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
					// 处理响应消息
					processResp(ctx, transportMsg);
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
	 * 处理响应消息
	 * @param ctx ChannelHandlerContext
	 * @param transportRespMsg 传输响应消息
	 */
	protected abstract void processResp(ChannelHandlerContext ctx, TransportMsg transportRespMsg);

}
