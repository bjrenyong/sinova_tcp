package sinova.tcp.framework.common.monitor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 以秒为单位的超速失败限速实现类<br>
 * 一般TCP请求端适合使用超速等待限速类进行限速，而接收者适合使用超速等待限速类进行限速<br>
 * factor: 速度值倍率，默认为1，即设置多少即实际限制多少；大于1表示设置多少实际限制更宽松，值越大越宽松；反之实际限制更严格<br>
 * 设置factor的原因：完全的标准限速往往谁都做不到，应该允许一定的范围浮动
 * @author Timothy
 */
public class SecondOverFalseSpeed implements IOverFalseSpeed {

	private static final Logger logger = LoggerFactory.getLogger(SecondOverFalseSpeed.class);

	/** 每秒速度值，依赖注入 */
	private int secondSpeed;
	/** 一秒被分割的份数，可注入修改，默认为1 */
	private int secondDivideNum = 1;
	/** 速度值倍率，可注入修改，默认为1 */
	private float factor = 1;

	/** 限速器运行标志 */
	private boolean runFlag = true;
	/** 速度计数器清零间隔时间 */
	private int clearIntervalMillis;
	/** 间隔时间计数上限 */
	private int intervalMillisCountMax;
	/** 速度计数器 */
	private AtomicInteger speedCount = new AtomicInteger(0);
	/** 计数的时间边界 */
	private AtomicLong time = new AtomicLong(System.currentTimeMillis());
	/** 监控的瞬时速度 */
	private int monitorSpeed = 0;
	/** 计时同步锁 */
	private byte[] timeLock = new byte[1];

	/**
	 * 初始化方法
	 */
	public void init() {
		// 速度计数器清零间隔时间初始化
		this.clearIntervalMillis = 1000 / secondDivideNum;
		// 间隔时间计数上限初始化
		this.intervalMillisCountMax = (int) ((secondSpeed / secondDivideNum) * factor);
		// 启动限速计数清零线程
		new SpeedCountClearThread().start();
	}

	/**
	 * 销毁方法
	 */
	public void destroy() {
		this.runFlag = false;
		synchronized (timeLock) {
			timeLock.notify();
		}
	}

	@Override
	public synchronized boolean limitSpeed() {
		while (runFlag) {
			if (speedCount.get() >= intervalMillisCountMax) {
				return false;
			} else {
				// 限速计数
				speedCount.incrementAndGet();
				return true;
			}
		}
		return false;
	}

	@Override
	public int getMonitorSpeed() {
		return monitorSpeed;
	}

	// ///////////////////////////////////////////////
	// 设置依赖注入方法begin

	public void setSecondSpeed(int secondSpeed) {
		this.secondSpeed = secondSpeed;
	}

	public void setSecondDivideNum(int secondDivideNum) {
		this.secondDivideNum = secondDivideNum;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}

	// 设置依赖注入方法end
	// ///////////////////////////////////////////////

	/**
	 * 限速计数清零线程类
	 * @author Timothy
	 */
	private class SpeedCountClearThread extends Thread {
		public void run() {
			// 这里初始化应该可以防止等待时间小于等于0的情况出现
			time.set(System.currentTimeMillis());
			while (runFlag) {
				try {
					// 发现只有这样控制才能保证各环境下的准确性
					long waitTime = clearIntervalMillis - System.currentTimeMillis() + time.get();
					if (waitTime > 0) {
						synchronized (timeLock) {
							timeLock.wait(waitTime);
						}
					}
					time.addAndGet(clearIntervalMillis);
					// 获取上一个瞬间的通过速度值，同时将速度计数器清零
					monitorSpeed = speedCount.getAndSet(0) * secondDivideNum;
					logger.debug("finish a speed count clear job!");
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

}
