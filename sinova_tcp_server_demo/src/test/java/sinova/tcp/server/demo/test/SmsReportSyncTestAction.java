package sinova.tcp.server.demo.test;

import io.netty.channel.Channel;
import sinova.tcp.framework.server.entity.ServerUserBase;
import sinova.tcp.framework.server.service.IServerConnectService;
import sinova.tcp.framework.test.ITestAction;
import sinova.tcp.protocol.standard.demo.SmsReportReq;

/**
 * 状态报告同步发送测试Action
 * @author Timothy
 */
public class SmsReportSyncTestAction implements ITestAction {

	private IServerConnectService<ServerUserBase> serverConnectService;
	private int userId;

	@Override
	public boolean isFinish() {
		return false;
	}

	@Override
	public void test(String commandStr) {
		String sendCountStr = commandStr.substring("sms sync report".length());
		int sendCount = Integer.parseInt(sendCountStr.trim());
		System.out.println("sync send sms report message test begin! sendCount=" + sendCount);
		for (int i = 0; i < sendCount; i++) {
			SmsReportReq smsReportReq = SmsReportTestService.createSmsReportReq(userId, "111");
			sendOneMsg(smsReportReq);
		}
		System.out.println("sync send sms report message test end! sendCount=" + sendCount);
	}

	private boolean isValidSendReqChannel(Channel channel) {
		if (channel == null) {
			return false;
		} else if (!channel.isActive()) {
			return false;
		} else {
			return true;
		}
	}

	private void sendOneMsg(SmsReportReq smsReportReq) {
		while (true) {
			Channel channel = serverConnectService.getOutBoundConnectMap().get(smsReportReq.getUserId());
			if (isValidSendReqChannel(channel)) {
				try {
					serverConnectService.businessReqSendSync(smsReportReq, channel);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			} else {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void setServerConnectService(IServerConnectService<ServerUserBase> serverConnectService) {
		this.serverConnectService = serverConnectService;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

}
