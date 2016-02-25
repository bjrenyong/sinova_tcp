package sinova.tcp.framework.server.handler;

import io.netty.channel.ChannelHandler.Sharable;

import org.springframework.beans.factory.annotation.Autowired;

import sinova.tcp.framework.server.service.IServerUserService;
import sinova.tcp.protocol.ILoginResp;
import sinova.tcp.protocol.simple.LoginResp;

/**
 * 服务端接收客户登录请求处理实现<br/>
 * 通过验证IP地址、用户名和密码进行登录验证
 * @author Timothy
 */
@Sharable
public class ServerReceiveLoginReqHandler extends AbsServerReceiveLoginReqHandler {

	/** 服务端用户信息服务 */
	@Autowired
	private IServerUserService serverUserService;

	@Override
	protected boolean isLoginSuccess(ILoginResp loginResp) {
		LoginResp resp = (LoginResp) loginResp;
		return resp.getSuccess();
	}

}
