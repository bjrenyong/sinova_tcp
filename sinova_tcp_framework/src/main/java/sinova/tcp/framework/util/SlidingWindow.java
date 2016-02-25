package sinova.tcp.framework.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.common.window.WindowMsg;

/**
 * 滑动窗口
 * @author Timothy
 */
public class SlidingWindow {

	private static final Logger logger = LoggerFactory.getLogger(SlidingWindow.class);

	/** 窗口大小 */
	private int windowSize;
	/** 窗口消息超时时间 */
	private int msgTimeOutSecond;
	/** 窗口消息序列-窗口消息MAP（此MAP大小固定） */
	private BoundedConcurrentMap<Integer, WindowMsg> window = null;

	public SlidingWindow(int windowSize, int msgTimeOutSecond) {
		this.windowSize = windowSize;
		this.msgTimeOutSecond = msgTimeOutSecond;
		this.window = new BoundedConcurrentMap<Integer, WindowMsg>(windowSize);
	}

	/**
	 * 向滑动窗口中放入窗口消息
	 * @param sequenceId 消息序列
	 * @param message 窗口消息
	 */
	public void putMsg(int sequenceId, WindowMsg message) {
		try {
			this.window.put(sequenceId, message);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 从滑动窗口移出某消息序列对应的窗口消息
	 * @param sequenceId 消息序列
	 * @return 消息序列对应的窗口消息(可能为null)
	 */
	public WindowMsg removeMsg(int sequenceId) {
		return this.window.remove(sequenceId);
	}

	/**
	 * 移除滑动窗口的所有窗口消息
	 * @return 所有移除的窗口消息集合
	 */
	public List<WindowMsg> removeAllMsg() {
		return window.removeAll();
	}

	/**
	 * 获取滑动窗口大小
	 * @return 滑动窗口大小
	 */
	public int getWindowSize() {
		return windowSize;
	}

	public int getMsgTimeOutSecond() {
		return msgTimeOutSecond;
	}

	/**
	 * 获取滑动窗口当前数据个数
	 * @return 滑动窗口当前数据个数
	 */
	public int getWindowRecordCount() {
		return this.window.getKeys().size();
	}

	public void setMsgTimeOutSecond(int msgTimeOutSecond) {
		this.msgTimeOutSecond = msgTimeOutSecond;
	}

	public BoundedConcurrentMap<Integer, WindowMsg> getWindow() {
		return window;
	}

	public void setWindow(BoundedConcurrentMap<Integer, WindowMsg> window) {
		this.window = window;
	}

}
