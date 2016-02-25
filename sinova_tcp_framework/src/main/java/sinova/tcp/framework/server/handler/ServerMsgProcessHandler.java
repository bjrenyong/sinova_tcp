package sinova.tcp.framework.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;

import org.springframework.beans.factory.annotation.Autowired;

import sinova.tcp.framework.common.FrameworkConstants;
import sinova.tcp.framework.common.handler.AbsMsgProcessHandler;
import sinova.tcp.framework.common.monitor.IOverFalseSpeed;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.common.window.WindowMsg;
import sinova.tcp.framework.server.entity.ServerUserBase;
import sinova.tcp.framework.server.service.IServerConnectService;
import sinova.tcp.framework.server.service.IServerSpeedService;
import sinova.tcp.framework.server.service.IServerUserService;

/**
 * 业务请求和业务响应的接收处理--服务端侧<br/>
 * 速度计数器在client和server的不同handler分别注入，有助于解决同一应用既有client端又有server端的情况使用注解
 * @author Timothy
 */
@Sharable
public class ServerMsgProcessHandler extends AbsMsgProcessHandler {

	/** 服务端连接服务 */
	@Autowired
	private IServerConnectService<ServerUserBase> serverConnectService;
	/** 服务端用户信息服务 */
	@Autowired
	private IServerUserService serverUserService;
	/** 服务端限速服务 */
	@Autowired
	private IServerSpeedService serverSpeedService;

	@Override
	public WindowMsg responseMatchWindowMsg(TransportMsg transportRespMsg, Channel channel) {
		return serverConnectService.responseMatchWindowMsg(transportRespMsg, channel);
	}

	@Override
	public int getUserId(Channel channel) {
		ServerUserBase userInfo = serverConnectService.getUserByChannel(channel);
		return userInfo.getUserId();
	}

	@Override
	protected void addReceiveCount(int userId, int count) {
		serverUserService.getServerUserByUserId(userId).getServerSpeedRecord().addReceiveTotal(count);
	}

	@Override
	protected boolean isAllowReceiveReq(int userId) {
		ServerUserBase serverUser = serverUserService.getServerUserByUserId(userId);
		// 对于全双工和仅下行的连接类型，服务端允许接收来自客户端的业务请求
		if (serverUser.getConnectionType() == FrameworkConstants.CONNECTION_TYPE_ALL
				|| serverUser.getConnectionType() == FrameworkConstants.CONNECTION_TYPE_MT) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean isAllowReceiveReqSpeed(int userId) {
		IOverFalseSpeed userReceiveSpeed = serverSpeedService.getReceiveSpeedByUserId(userId);
		if (userReceiveSpeed == null) {
			// 没有相应的服务端业务请求接收限速器，即代表不做限速
			return true;
		}
		return userReceiveSpeed.limitSpeed();
	}

}
