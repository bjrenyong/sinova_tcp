package sinova.tcp.framework.common.window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.common.protocol.TransportMsg;

/**
 * 同步滑动窗口消息<br>
 * 在滑动窗口消息的基础上实现了IResponseFuture
 * @author Timothy
 */
public class SyncWindowMsg extends WindowMsg implements IResponseFuture {

	private static final Logger logger = LoggerFactory.getLogger(SyncWindowMsg.class);

	/** 同步请求超时时间 */
	private static final int TIMEOUT_MILLIS = 40000;
	/** 传输响应消息 */
	private TransportMsg transportRespMsg;
	/** 同步请求完成标志 */
	private boolean downFlag = false;
	/** 同步等待锁 */
	private byte[] lock = new byte[1];

	public SyncWindowMsg(TransportMsg transportReqMsg) {
		super(transportReqMsg);
	}

	@Override
	public TransportMsg get() {
		return get(TIMEOUT_MILLIS);
	}

	@Override
	public TransportMsg get(int timeoutInMillis) {
		while (!isDone()) {
			synchronized (lock) {
				try {
					lock.wait(5);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (System.currentTimeMillis() - this.getStart() >= timeoutInMillis) {
				break;
			}
		}
		return this.transportRespMsg;
	}

	@Override
	public boolean isDone() {
		return downFlag;
	}

	/**
	 * 回写传输响应消息并唤醒同步请求线程
	 * @param transportRespMsg
	 */
	public void setTransportRespMsg(TransportMsg transportRespMsg) {
		this.transportRespMsg = transportRespMsg;
		this.downFlag = true;
		synchronized (lock) {
			lock.notify();
		}
	}

}
