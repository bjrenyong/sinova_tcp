package sinova.tcp.framework.server.handler;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sinova.tcp.framework.common.handler.TransportReqMsgChannelInboundHandler;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.server.service.IServerConnectService;
import sinova.tcp.protocol.ILoginReq;
import sinova.tcp.protocol.ILoginResp;
import sinova.tcp.protocol.IResp;

/**
 * 服务端接收客户登录请求处理抽象类
 * @author Timothy
 */
public abstract class AbsServerReceiveLoginReqHandler extends TransportReqMsgChannelInboundHandler<ILoginReq> {

	private static final Logger logger = LoggerFactory.getLogger(AbsServerReceiveLoginReqHandler.class);

	/** 服务端的Netty连接管理，依赖注入 */
	@Autowired
	private IServerConnectService<?> serverConnectService;

	@Override
	protected IResp processReqReturnResp(ChannelHandlerContext ctx, TransportMsg transportReqMsg, ILoginReq reqMsg) {
		// 请求登录Action
		ILoginResp resp = serverConnectService.login(reqMsg, ctx.channel());
		return resp;
	}

	/**
	 * 发送登录响应信息后的处理
	 * @param ctx ChannelHandlerContext
	 * @param transportReqMsg 传输请求消息
	 * @param respMsg 响应消息
	 */
	@Override
	protected void doAfterSendResp(ChannelHandlerContext ctx, TransportMsg transportReqMsg, IResp respMsg) {
		ILoginResp resp = (ILoginResp) respMsg;
		// 如果resp是登录失败响应，则关闭连接
		if (!isLoginSuccess(resp)) {
			logger.info("login failed, close channel! channel=" + ctx.channel());
			ctx.close();
		}
	}

	/**
	 * 判断是否为登录成功
	 * @param loginResp 登录响应消息
	 * @return 是否为登录成功
	 */
	protected abstract boolean isLoginSuccess(ILoginResp loginResp);
}
