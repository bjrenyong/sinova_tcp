package sinova.tcp.server.demo.action;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.action.IReqAction;
import sinova.tcp.framework.common.exception.BusinessException;
import sinova.tcp.protocol.standard.demo.SmsMtReq;
import sinova.tcp.protocol.standard.demo.SmsMtResp;

/**
 * 短信下行请求处理Action<br/>
 * 用于业务测试，仅供参考
 * @author Timothy
 */
public class SmsMtReqAction implements IReqAction<SmsMtReq, SmsMtResp> {

	private static final Logger logger = LoggerFactory.getLogger(SmsMtReqAction.class);
	private static final AtomicLong AL_SYS_SEQUENCE = new AtomicLong();

	@Override
	public SmsMtResp action(SmsMtReq request, int userId) throws BusinessException {
		logger.info("receive a sms mt request, userId=" + userId + ", smsMtReq=" + request);
		// 如果SmsMtReq中的clientMtSequence<0，抛出ActionProcessException，用于测试系统对于ActionProcessException的处理
		if (request.getClientMtSequence() < 0) {
			logger.warn("receive a sms mt request, but clientMtSequence is negative, smsMtReq=" + request);
			throw new BusinessException("0001", "client mt sequence shouldn't be negative");
		}
		SmsMtResp smsMtResp = new SmsMtResp();
		smsMtResp.setClientMtSequence(request.getClientMtSequence());
		smsMtResp.setSysMtSequence(AL_SYS_SEQUENCE.incrementAndGet());
		logger.info("return a sms mt response, smsMtResp=" + smsMtResp);
		return smsMtResp;
	}

}
