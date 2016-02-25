package sinova.tcp.framework.server.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sinova.tcp.framework.common.handler.TransportReqMsgChannelInboundHandler;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.server.service.IServerConnectService;
import sinova.tcp.protocol.ILogoutReq;
import sinova.tcp.protocol.IResp;

/**
 * 服务端接收客户登出请求处理
 * @author Timothy
 */
@Sharable
public class ServerReceiveLogoutReqHandler extends TransportReqMsgChannelInboundHandler<ILogoutReq> {

	private static final Logger logger = LoggerFactory.getLogger(ServerReceiveLogoutReqHandler.class);

	/** 服务端Netty连接管理，依赖注入 */
	@Autowired
	private IServerConnectService<?> serverConnectService;

	@Override
	protected IResp processReqReturnResp(ChannelHandlerContext ctx, TransportMsg transportReqMsg, ILogoutReq reqMsg) {
		// 服务端连接管理执行登出操作
		serverConnectService.logout(ctx.channel());
		// 没有登出响应消息，返回null
		return null;
	}

	@Override
	protected void doAfterSendResp(ChannelHandlerContext ctx, TransportMsg transportReqMsg, IResp respMsg) {
		logger.info("receive client logout request, connection close!");
		ctx.close();
	}

}
