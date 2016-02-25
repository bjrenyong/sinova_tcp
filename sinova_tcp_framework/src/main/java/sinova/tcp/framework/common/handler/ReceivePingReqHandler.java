package sinova.tcp.framework.common.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.protocol.IPingReq;
import sinova.tcp.protocol.IPingResp;
import sinova.tcp.protocol.IResp;
import sinova.tcp.protocol.simple.PingResp;

/**
 * 接收心跳请求处理器
 * @author Timothy
 */
@Sharable
public class ReceivePingReqHandler extends TransportReqMsgChannelInboundHandler<IPingReq> {

	private static final Logger logger=LoggerFactory.getLogger(ReceivePingReqHandler.class);
	
	private IPingResp pingResp=new PingResp();
	
	@Override
	protected IResp processReqReturnResp(ChannelHandlerContext ctx, TransportMsg transportReqMsg, IPingReq reqMsg) {
		logger.info("receive ping request, channel={}",ctx.channel());
		return pingResp;
	}

	@Override
	protected void doAfterSendResp(ChannelHandlerContext ctx, TransportMsg transportReqMsg, IResp respMsg) {
	}

}
