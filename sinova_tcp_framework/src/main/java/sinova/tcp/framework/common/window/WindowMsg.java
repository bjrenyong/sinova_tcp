package sinova.tcp.framework.common.window;

import sinova.tcp.framework.common.protocol.TransportMsg;

/**
 * 滑动窗口消息
 * @author Timothy
 */
public class WindowMsg {

	/** 传输消息 */
	private TransportMsg transportReqMsg;
	/** 传输消息发送时间戳 */
	private final long start = System.currentTimeMillis();

	public WindowMsg(TransportMsg transportReqMsg) {
		this.transportReqMsg = transportReqMsg;
	}

	public TransportMsg getTransportReqMsg() {
		return transportReqMsg;
	}

	public long getStart() {
		return start;
	}

}
