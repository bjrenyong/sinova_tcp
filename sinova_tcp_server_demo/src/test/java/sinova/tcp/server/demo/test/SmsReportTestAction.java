package sinova.tcp.server.demo.test;

import sinova.tcp.framework.test.ITestAction;

public class SmsReportTestAction implements ITestAction {

	private int[] userIdArr;
	private SmsReportQueueProducer smsReportQueueProducer;

	@Override
	public boolean isFinish() {
		return false;
	}

	@Override
	public void test(String commandStr) {
		String sendCountStr = commandStr.substring("sms report".length());
		int sendCount = Integer.parseInt(sendCountStr.trim());
		System.out.println("send sms report message test begin! sendCount=" + (sendCount * userIdArr.length));
		smsReportQueueProducer.sendMessages(userIdArr, sendCount);
		System.out.println("send sms mt message test end! sendCount=" + (sendCount * userIdArr.length));
	}

	public void setUserIdArr(int[] userIdArr) {
		this.userIdArr = userIdArr;
	}

	public void setSmsReportQueueProducer(SmsReportQueueProducer smsReportQueueProducer) {
		this.smsReportQueueProducer = smsReportQueueProducer;
	}

}
