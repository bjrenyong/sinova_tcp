package sinova.tcp.client.demo.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.client.service.ClientConnectService;
import sinova.tcp.framework.test.ITestAction;

/**
 * netty关闭连接测试
 * @author Timothy
 */
@Service
public class NettyCloseTestAction implements ITestAction {

	@Autowired
	private ClientConnectService clientConnectService;

	@Override
	public boolean isFinish() {
		return false;
	}

	@Override
	public void test(String commandStr) {
		clientConnectService.connect2disconnect();
	}

}
