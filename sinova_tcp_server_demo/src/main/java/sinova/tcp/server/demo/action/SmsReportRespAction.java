package sinova.tcp.server.demo.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.action.IRespAction;
import sinova.tcp.protocol.standard.demo.SmsReportReq;
import sinova.tcp.protocol.standard.demo.SmsReportResp;

/**
 * 短信状态报告响应处理Action<br/>
 * 用于业务测试，仅供参考
 * @author Timothy
 */
public class SmsReportRespAction implements IRespAction<SmsReportReq, SmsReportResp> {

	private static final Logger logger = LoggerFactory.getLogger(SmsReportRespAction.class);

	@Override
	public void action(SmsReportReq request, SmsReportResp response) {
		logger.info("receive a sms report response! response={}", response);
	}
}
