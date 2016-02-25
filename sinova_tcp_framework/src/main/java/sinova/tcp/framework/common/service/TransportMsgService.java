package sinova.tcp.framework.common.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.protocol.IReq;

/**
 * 传输消息服务
 * @author Timothy
 */
@Service
public class TransportMsgService {

	private static final Logger logger = LoggerFactory.getLogger(TransportMsgService.class);
	/** 请求类消息的序列号生成器 */
	private static final AtomicInteger AI_SEQUENCEID = new AtomicInteger();

	@Autowired
	private ICommandReferenceService commandReferenceService;

	/**
	 * 请求消息封装成传输消息
	 * @param msg 请求消息
	 * @return 传输消息
	 */
	public TransportMsg req2TransportMsg(IReq msg) {
		if (commandReferenceService.getCommandIdByMsgClass(msg.getClass()) == null) {
			logger.warn("Req2TransportMsg failed, can't get message's commandId! msg=" + msg);
			return null;
		}
		TransportMsg transportMsg = new TransportMsg();
		// 根据请求消息类类型获取对应的请求消息ID
		transportMsg.setCommandId(commandReferenceService.getCommandIdByMsgClass(msg.getClass()));
		// 请求类消息使用新的序列号
		transportMsg.setSequenceId(AI_SEQUENCEID.incrementAndGet());
		transportMsg.setMsg(msg);
		return transportMsg;
	}
}
