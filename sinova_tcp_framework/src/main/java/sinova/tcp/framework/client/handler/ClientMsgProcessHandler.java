package sinova.tcp.framework.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.client.service.ClientConnectService;
import sinova.tcp.framework.client.service.IClientUserService;
import sinova.tcp.framework.common.handler.AbsMsgProcessHandler;
import sinova.tcp.framework.common.monitor.IOverFalseSpeed;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.common.window.WindowMsg;

/**
 * 业务请求和业务响应的接收处理器--客户端侧<br/>
 * 速度计数器在client和server的不同handler分别注入，有助于解决同一应用既有client端又有server端的情况使用注解
 * @author Timothy
 */
@Service
@Sharable
public class ClientMsgProcessHandler extends AbsMsgProcessHandler {

	@Autowired
	private ClientConnectService clientConnectService;
	@Autowired
	private IClientUserService clientUserService;
	/** 客户端接收限速开关，默认开启限速 */
	@Value("${netty.client.receive.speed_limit_flag:true}")
	private boolean clientReceiveSpeedLimitFlag;
	/** 客户端接收限速器 */
	@Resource
	private IOverFalseSpeed clientReceiveSecondSpeed;

	@Override
	protected WindowMsg responseMatchWindowMsg(TransportMsg transportRespMsg, Channel channel) {
		// 匹配取出滑动窗口中传输响应信息对应的窗口消息
		return clientConnectService.responseMatchWindowMsg(transportRespMsg);
	}

	@Override
	protected int getUserId(Channel channel) {
		return clientUserService.getUserId();
	}

	@Override
	protected void addReceiveCount(int userId, int count) {
		clientUserService.getClientSpeedRecord().addReceiveTotal(count);
	}

	@Override
	protected boolean isAllowReceiveReq(int userId) {
		return clientConnectService.isAllowReqReceive();
	}

	@Override
	protected boolean isAllowReceiveReqSpeed(int userId) {
		if (clientReceiveSpeedLimitFlag && clientReceiveSecondSpeed != null) {
			// 执行限速
			return clientReceiveSecondSpeed.limitSpeed();
		}
		// 不执行限速
		return true;
	}

}
