package sinova.tcp.framework.client.monitor;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.client.service.ClientConnectService;
import sinova.tcp.framework.client.service.IClientUserService;
import sinova.tcp.framework.client.service.ClientConnectService.ConnectStatus;
import sinova.tcp.framework.common.monitor.BaseMonitorRunnable;

/**
 * client tcp速度监控器
 * @author Timothy
 */
@Service
public class ClientTcpSpeedMonitor extends BaseMonitorRunnable {

	@Resource
	private IClientUserService clientUserService;
	@Autowired
	private ClientConnectService clientConnectService;

	@Override
	public void init() {
		// do nothing
	}

	@Override
	public void clear() {
		// do nothing
	}

	@Override
	public void doOneMonitor() {
		// 采集连接状态
		clientUserService.getClientSpeedRecord().setConnectStatus(
				clientConnectService.checkConnectStatus() == ConnectStatus.CONNECT);
		// 速度采集并将计数清零
		clientUserService.getClientSpeedRecord().setAndClearRecord();
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public Map<String, Object> getMonitorResult() {
		Map<String, Object> monitorResult = new HashMap<String, Object>();
		monitorResult.put("clientSpeedRecord", clientUserService.getClientSpeedRecord().getRecord());
		return monitorResult;
	}

}
