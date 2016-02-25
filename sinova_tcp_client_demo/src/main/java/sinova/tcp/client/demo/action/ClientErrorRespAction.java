package sinova.tcp.client.demo.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sinova.tcp.framework.action.IRespAction;
import sinova.tcp.framework.client.service.ClientConnectService;
import sinova.tcp.framework.common.TcpErrorCode;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.simple.ErrorResp;

/**
 * 这个示例演示了对于流控错误的消息如何进行重发
 * @author Timothy
 */
public class ClientErrorRespAction implements IRespAction<IReq, ErrorResp> {

	private static final Logger logger = LoggerFactory.getLogger(ClientErrorRespAction.class);

	@Autowired
	private ClientConnectService clientConnectService;

	@Override
	public void action(IReq request, ErrorResp response) {
		logger.error("receive error response: response=" + response + ", request=" + request);
		if (request != null && TcpErrorCode.ERRORCODE_SPEED_OVERFLOW.getErrorCode().equals(response.getErrorCode())) {
			/*
			 * 这里将流控的请求做了简单的重发，但是实际上往往不该这么做，最好把待重发的消息放到相应的非阻塞队列中<br>
			 * 所有的响应action使用的是NioEventLoopGroup线程，需要保证尽快的处理以释放NioEventLoopGroup线程，否则它可能成为瓶颈！<br>
			 * 发送请求受流控影响，它有可能阻塞，因此有可能导致NioEventLoopGroup线程的阻塞<br>
			 * 如果你发现ErrorResp对应的request为空，很可能产生阻塞了！
			 */
			logger.info("request send speed overflow and resend again! request=" + request);
			try {
				clientConnectService.businessReqSendAsync(request);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
