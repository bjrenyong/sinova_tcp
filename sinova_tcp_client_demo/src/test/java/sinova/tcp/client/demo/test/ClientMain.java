package sinova.tcp.client.demo.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import sinova.tcp.framework.common.TcpAppStatus;
import sinova.tcp.framework.test.ITestAction;
import sinova.tcp.framework.test.TestCommand;
import sinova.tcp.framework.test.TestCommandService;

public class ClientMain {

	public static void main(String[] args) {

		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("tcp_client_all.xml");
		TcpAppStatus tcpAppStatus = (TcpAppStatus) ac.getBean("tcpAppStatus");
		tcpAppStatus.setStatus(TcpAppStatus.Status.STATUS_ACTIVE);

		SmsMtQueueProducer smsMtQueueProducer = (SmsMtQueueProducer) ac.getBean("smsMtQueueProducer");
		smsMtQueueProducer.sendOneMsg();

		TestCommandService testCommandService = (TestCommandService) ac.getBean("testCommandService");
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		String cmdStr = null;
		System.out.println("请输入指令");
		try {
			while (true) {
				cmdStr = br.readLine();
				if (cmdStr == null) {
					continue;
				} else if (processCmd(cmdStr, testCommandService)) {
					// 处理结果要求系统中断
					break;
				} else {
					System.out.println("请输入指令");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		tcpAppStatus.setStatus(TcpAppStatus.Status.STAUTS_CLOSING);
		ac.close();
		tcpAppStatus.setStatus(TcpAppStatus.Status.STAUTS_CLOSED);
	}

	private static boolean processCmd(String cmdStr, TestCommandService testCommandService) {
		TestCommand testCommand = testCommandService.matchTestCommand(cmdStr);
		if (testCommand == null) {
			testCommandService.printHelpMsg(System.err);
			return false;
		} else {
			ITestAction testAction = testCommand.getTestAction();
			testAction.test(cmdStr);
			return testAction.isFinish();
		}
	}

}
