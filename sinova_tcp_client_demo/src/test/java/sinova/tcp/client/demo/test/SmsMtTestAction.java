package sinova.tcp.client.demo.test;

import sinova.tcp.framework.test.ITestAction;

public class SmsMtTestAction implements ITestAction {

	private String smsContent;
	private long MobileNumBegin;
	private SmsMtQueueProducer smsMtQueueProducer;

	@Override
	public boolean isFinish() {
		return false;
	}

	@Override
	public void test(String commandStr) {
		String sendCountStr = commandStr.substring("sms mt".length());
		int sendCount = Integer.parseInt(sendCountStr.trim());
		System.out.println("send sms mt message test begin! sendCount=" + sendCount);
		smsMtQueueProducer.sendMessages(MobileNumBegin, smsContent, sendCount);
		System.out.println("send sms mt message test end! sendCount=" + sendCount);
	}

	public void setSmsContent(String smsContent) {
		this.smsContent = smsContent;
	}

	public void setMobileNumBegin(long mobileNumBegin) {
		MobileNumBegin = mobileNumBegin;
	}

	public void setSmsMtQueueProducer(SmsMtQueueProducer smsMtQueueProducer) {
		this.smsMtQueueProducer = smsMtQueueProducer;
	}

}
