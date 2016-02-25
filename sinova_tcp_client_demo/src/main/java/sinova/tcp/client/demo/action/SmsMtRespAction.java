package sinova.tcp.client.demo.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.action.IRespAction;
import sinova.tcp.protocol.standard.demo.SmsMtReq;
import sinova.tcp.protocol.standard.demo.SmsMtResp;

/**
 * 短信下行响应处理Action<br/>
 * 用于业务测试，仅供参考
 * @author Timothy
 */
public class SmsMtRespAction implements IRespAction<SmsMtReq, SmsMtResp> {

	private static final Logger logger = LoggerFactory.getLogger(SmsMtRespAction.class);

	@Override
	public void action(SmsMtReq request, SmsMtResp response) {
		logger.info("receive a sms mt response! smsMtResp=" + response);
	}

}
