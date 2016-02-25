package sinova.tcp.client.demo.test;

import java.util.concurrent.atomic.AtomicLong;

import sinova.tcp.protocol.standard.demo.SmsMtReq;

public class SmsMtTestService {

	private static final AtomicLong AI_CLIENT_SEQUENCE = new AtomicLong();

	public static SmsMtReq createSmsMtReq(long MobileNum, String smsContent, int priority) {
		return createSmsMtReq(AI_CLIENT_SEQUENCE.incrementAndGet(), MobileNum, smsContent, priority);
	}

	public static SmsMtReq createSmsMtReq(long clientMtSequence, long MobileNum, String smsContent, int priority) {
		SmsMtReq mtReq = new SmsMtReq();
		mtReq.setClientMtSequence(clientMtSequence);
		mtReq.setMobileNum(MobileNum + "");
		mtReq.setSmsContent(smsContent);
		mtReq.setPriority(priority);
		return mtReq;
	}
}
