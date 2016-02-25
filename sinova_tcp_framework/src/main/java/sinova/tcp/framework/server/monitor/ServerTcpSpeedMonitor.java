package sinova.tcp.framework.server.monitor;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.common.monitor.BaseMonitorRunnable;
import sinova.tcp.framework.server.entity.ServerUserBase;
import sinova.tcp.framework.server.service.IServerConnectService;
import sinova.tcp.framework.server.service.IServerUserService;

/**
 * 服务端TCP速度监控器<br/>
 * @author Timothy
 */
@Service
public class ServerTcpSpeedMonitor extends BaseMonitorRunnable {

	/** 服务端用户信息服务 */
	@Autowired
	private IServerUserService serverUserService;
	@Autowired
	private IServerConnectService<?> serverConnectService;

	@Override
	public void init() {
		// do nothing
	}

	@Override
	public void clear() {
		// do nothing
	}

	/**
	 * 获取某用户通道的连接状态
	 * @param userId 用户ID
	 * @return 通道的连接状态, true:连接; false:未连接
	 */
	private boolean validateUserChannel(Integer userId) {
		Channel channel = serverConnectService.getChannelByUserId(userId);
		if (channel != null && channel.isActive()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void doOneMonitor() {
		Map<Integer, ServerUserBase> id2UserInfoMap = serverUserService.getId2UserInfoMap();
		for (ServerUserBase userInfo : id2UserInfoMap.values()) {
			// 采集连接状态
			userInfo.getServerSpeedRecord().setConnectStatus(validateUserChannel(userInfo.getUserId()));
			// 速度采集并将计数清零
			userInfo.getServerSpeedRecord().setAndClearRecord();
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public Map<String, Object> getMonitorResult() {
		Map<String, Object> monitorResult = new HashMap<String, Object>();
		Map<Integer, ServerUserBase> id2UserInfoMap = serverUserService.getId2UserInfoMap();
		for (ServerUserBase userInfo : id2UserInfoMap.values()) {
			monitorResult.put(userInfo.getUserId() + "", userInfo.getServerSpeedRecord().getRecord());
		}
		return monitorResult;
	}

}
