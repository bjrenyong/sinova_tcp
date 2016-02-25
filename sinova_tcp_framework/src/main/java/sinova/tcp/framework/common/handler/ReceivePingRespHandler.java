package sinova.tcp.framework.common.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.protocol.IPingResp;

/**
 * 接收心跳响应处理器
 * @author Timothy
 */
@Sharable
public class ReceivePingRespHandler extends TransportRespMsgChannelInboundHandler<IPingResp> {

	private static final Logger logger = LoggerFactory.getLogger(ReceivePingRespHandler.class);

	@Override
	protected void processResp(ChannelHandlerContext ctx, TransportMsg transportRespMsg) {
		logger.info("receive ping response, channel={}" , ctx.channel());
	}

}
