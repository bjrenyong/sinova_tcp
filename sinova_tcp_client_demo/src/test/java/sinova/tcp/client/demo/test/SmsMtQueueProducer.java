package sinova.tcp.client.demo.test;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.standard.demo.SmsMtReq;
import sinovatech.components.msgqueue.IQueueProduceConsumeClient;

@Service
public class SmsMtQueueProducer {

	private static final Logger logger = LoggerFactory.getLogger(SmsMtQueueProducer.class);
	private static final AtomicLong AI_CLIENT_SEQUENCE = new AtomicLong();

	@Autowired
	private IQueueProduceConsumeClient<IReq> smsMtQueue;

	public void sendOneMsg() {
		SmsMtReq mtReq = new SmsMtReq();
		mtReq.setClientMtSequence(AI_CLIENT_SEQUENCE.incrementAndGet());
		mtReq.setMobileNum("18515888353");
		mtReq.setSmsContent("sms content");
		mtReq.setPriority(1);
		smsMtQueue.send(mtReq);
		logger.info("send a message to the smsMtQueue! mtReq=" + mtReq);
	}

	public void sendMessages(long MobileNumBegin, String smsContent, int sendCount) {
		long mobileNum = MobileNumBegin;
		for (int i = 0; i < sendCount; i++) {
			SmsMtReq mtReq = new SmsMtReq();
			mtReq.setClientMtSequence(AI_CLIENT_SEQUENCE.incrementAndGet());
			mtReq.setMobileNum(mobileNum + "");
			mtReq.setSmsContent(smsContent);
			mtReq.setPriority(1);
			smsMtQueue.send(mtReq);
			mobileNum++;
			logger.info("send a message to the smsMtQueue! mtReq=" + mtReq);
		}
	}
}
