package sinova.tcp.server.demo.test;

import java.util.concurrent.atomic.AtomicLong;

import sinova.tcp.protocol.standard.demo.SmsReportReq;

public class SmsReportTestService {

	private static final AtomicLong AL_SEQUENCE = new AtomicLong();

	public static SmsReportReq createSmsReportReq(int userId, String reportCode) {
		return createSmsReportReq(userId, reportCode, AL_SEQUENCE.incrementAndGet(), AL_SEQUENCE.incrementAndGet());
	}

	public static SmsReportReq createSmsReportReq(int userId, String reportCode, long clientMtSequence, long sysMtSequence) {
		SmsReportReq mtReq = new SmsReportReq();
		mtReq.setClientMtSequence(clientMtSequence);
		mtReq.setSysMtSequence(sysMtSequence);
		mtReq.setUserId(userId);
		mtReq.setReportCode(reportCode);
		return mtReq;
	}
}
