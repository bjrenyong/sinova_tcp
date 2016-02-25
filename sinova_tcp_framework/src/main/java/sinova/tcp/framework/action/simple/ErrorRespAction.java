package sinova.tcp.framework.action.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.action.IRespAction;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.simple.ErrorResp;

/**
 * 错误响应Action-缺省实现<br/>
 * @author Timothy
 */
public class ErrorRespAction implements IRespAction<IReq, ErrorResp> {

	private static final Logger logger = LoggerFactory.getLogger(ErrorRespAction.class);

	@Override
	public void action(IReq request, ErrorResp response) {
		logger.error("receive error response: response=" + response + ", request=" + request);
	}

}
