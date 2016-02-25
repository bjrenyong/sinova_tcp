package sinova.tcp.client.demo.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.action.IReqAction;
import sinova.tcp.framework.common.exception.BusinessException;
import sinova.tcp.protocol.standard.demo.SmsReportReq;
import sinova.tcp.protocol.standard.demo.SmsReportResp;

/**
 * 短信状态报告请求处理Action<br/>
 * 用于业务测试，仅供参考
 * @author Timothy
 */
public class SmsReportReqAction implements IReqAction<SmsReportReq, SmsReportResp> {

	private static final Logger logger = LoggerFactory.getLogger(SmsReportReqAction.class);

	@Override
	public SmsReportResp action(SmsReportReq request, int userId) throws BusinessException {
		logger.info("receive a sms report request, userId=" + userId + ", smsReportReq=" + request);
		if (request.getClientMtSequence() < 0) {
			logger.warn("receive a sms report request, but clientMtSequence is negative");
			throw new BusinessException("0001", "client mt sequence shouldn't be negative");
		}
		SmsReportResp response = new SmsReportResp();
		response.setClientMtSequence(request.getClientMtSequence());
		response.setSysMtSequence(request.getSysMtSequence());
		logger.info("return a sms report response, smsReportResp=" + response);
		return response;
	}

}
