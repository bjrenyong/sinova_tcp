package sinova.tcp.framework.common.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import sinova.tcp.protocol.IReq;

/**
 * 异步请求消息发送线程基类
 * @author Timothy
 */
@Service
public abstract class AsyncReqSendThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(AsyncReqSendThread.class);

	/** 同步锁 */
	protected byte[] lock = new byte[1];
	private boolean runFlag = true;

	public void run() {
		logger.info("async request message send thread begin! threadName=" + this.getThreadName());
		while (runFlag) {
			try {
				// 判断是否允许发送消息
				if (isValid2Send()) {
					// 从请求队列获取请求消息
					IReq reqMsg = getReqMsgFromQueue();
					if (reqMsg == null) {
						// 没有取得可发送的数据，进行休眠
						logger.info("get nothig from queue, thread sleep! threadName=" + this.getThreadName());
						this.waitMillis(getSendSleepMillis());
					} else if (isValidMsg(reqMsg)) {
						sendReqMsg(reqMsg);
					} else {
						// 非有效的请求消息处理
						doWhenNotValidReqMsg(reqMsg);
					}
				} else {
					// 当前不允许从队列获取请求消息
					logger.info("disallow receive message from queue, sleep! threadName=" + this.getThreadName());
					this.waitMillis(getSendSleepMillis());
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				this.waitMillis(getSendSleepMillis());
			}
		}
		logger.info("async request message send thread end! threadName=" + this.getThreadName());
	}

	protected void setRunFlag(boolean runFlag) {
		this.runFlag = runFlag;
	}

	protected void waitMillis(long timeMillis) {
		synchronized (lock) {
			try {
				lock.wait(timeMillis);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 唤醒此异步请求消息发送线程
	 */
	protected void notifyService() {
		synchronized (lock) {
			lock.notify();
		}
	}

	/**
	 * 获取请求发送线程名
	 * @return 请求发送线程名
	 */
	public abstract String getThreadName();

	/**
	 * 当前是否允许发送消息
	 * @return boolean
	 */
	public abstract boolean isValid2Send();

	/**
	 * 从请求队列获取请求消息
	 * @return 请求消息
	 */
	public abstract IReq getReqMsgFromQueue();

	/**
	 * 获取发送休眠的毫秒值
	 * @return 发送休眠的毫秒值
	 */
	public abstract long getSendSleepMillis();

	/**
	 * 检查发送消息的有效性
	 * @param reqMsg 请求消息
	 * @return boolean
	 */
	public abstract boolean isValidMsg(IReq reqMsg);

	/**
	 * 发送请求消息
	 * @param reqMsg 请求消息
	 */
	public abstract void sendReqMsg(IReq reqMsg);

	/**
	 * 处理无法识别的请求消息
	 * @param reqMsg 请求消息
	 */
	public abstract void doWhenNotValidReqMsg(IReq reqMsg);
}
