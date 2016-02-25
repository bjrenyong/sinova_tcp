package sinova.tcp.server.demo.test;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.standard.demo.SmsReportReq;
import sinovatech.components.msgqueue.IQueueProduceConsumeClient;

@Service
public class SmsReportQueueProducer {

	private static final Logger logger = LoggerFactory.getLogger(SmsReportQueueProducer.class);
	private static final AtomicLong AL_SEQUENCE = new AtomicLong();

	@Autowired
	private IQueueProduceConsumeClient<IReq> smsReportQueue;

	public void sendOneMsg() {
		SmsReportReq reportReq = createOneMsg(1);
		smsReportQueue.send(reportReq);
		logger.info("send a report message to the smsReportQueue! reportReq=" + reportReq);
	}

	public void sendMessages(int[] userIdArr, int oneSendCount) {
		for (int i = 0; i < oneSendCount; i++) {
			for (int userId : userIdArr) {
				SmsReportReq reportReq = createOneMsg(userId);
				smsReportQueue.send(reportReq);
				logger.info("send a report message to the smsReportQueue! reportReq=" + reportReq);
			}
		}
	}

	private SmsReportReq createOneMsg(int userId) {
		SmsReportReq reportReq = new SmsReportReq();
		reportReq.setUserId(userId);
		reportReq.setClientMtSequence(AL_SEQUENCE.incrementAndGet());
		reportReq.setSysMtSequence(AL_SEQUENCE.incrementAndGet());
		reportReq.setReportCode("111");
		return reportReq;
	}
}
