package sinova.tcp.client.demo.test;

import sinova.tcp.framework.client.service.ClientConnectService;
import sinova.tcp.framework.test.ITestAction;
import sinova.tcp.protocol.standard.demo.SmsMtReq;

/**
 * 短信同步发送测试Action
 * @author Timothy
 */
public class SmsMtSyncTestAction implements ITestAction {

	private ClientConnectService clientConnectService;
	private String smsContent;
	private long mobileNumBegin;

	@Override
	public boolean isFinish() {
		return false;
	}

	@Override
	public void test(String commandStr) {
		String sendCountStr = commandStr.substring("sms sync mt".length());
		int sendCount = Integer.parseInt(sendCountStr.trim());
		System.out.println("sync send sms mt message test begin! sendCount=" + sendCount);
		long mobileNum = mobileNumBegin;
		int count = 0;
		while (clientConnectService.isAllowRunStatus() && count < sendCount) {
			SmsMtReq smsMtReq = SmsMtTestService.createSmsMtReq(mobileNum, smsContent, 1);
			sendOneMsg(smsMtReq);
			mobileNum++;
			count++;
		}
		System.out.println("sync send sms mt message test end! sendCount=" + sendCount);
	}

	private void sendOneMsg(SmsMtReq smsMtReq) {
		while (clientConnectService.isAllowRunStatus()) {
			if (clientConnectService.isTcpConnect()) {
				try {
					clientConnectService.businessReqSendSync(smsMtReq);
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

	public void setClientConnectService(ClientConnectService clientConnectService) {
		this.clientConnectService = clientConnectService;
	}

	public void setSmsContent(String smsContent) {
		this.smsContent = smsContent;
	}

	public void setMobileNumBegin(long mobileNumBegin) {
		this.mobileNumBegin = mobileNumBegin;
	}
	
}
