package sinova.tcp.framework.client.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sinova.tcp.framework.client.service.ClientConnectService;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.protocol.simple.LoginResp;

/**
 * 登录响应处理handler
 * @author Timothy
 */
@Sharable
public class LoginResponseHandler extends AbsClientReceiveLoginRespHandler {

	private static final Logger logger = LoggerFactory.getLogger(LoginResponseHandler.class);

	@Autowired
	private ClientConnectService clientConnectService;

	@Override
	protected void processResp(ChannelHandlerContext ctx, TransportMsg transportRespMsg) {
		LoginResp loginResp = (LoginResp) transportRespMsg.getMsg();
		logger.info("receive login response, loginResp={}", loginResp);
		if (loginResp.getSuccess().booleanValue() == true) { 
			// 设置连接类型
			clientConnectService.setConnectionType(loginResp.getConnectionType());
			// 设置连接通道
			clientConnectService.setChannel(ctx.channel());
		} else {
			ctx.close();
		}
	}

}
