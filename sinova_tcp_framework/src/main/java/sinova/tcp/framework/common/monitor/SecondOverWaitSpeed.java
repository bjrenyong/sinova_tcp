package sinova.tcp.framework.common.monitor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 以秒为单位的超速等待限速实现类<br/>
 * 将1秒分割为若干等份以保证发送速度的均匀<br/>
 * 如：将一秒分为2个500毫秒<br/>
 * 一般TCP请求端适合使用超速等待限速类进行限速，而接收者适合使用超速等待限速类进行限速
 * @author Timothy
 */
public class SecondOverWaitSpeed implements IOverWaitSpeed {

	private static final Logger logger = LoggerFactory.getLogger(SecondOverWaitSpeed.class);
	private static final long WAIT_MILLIS = 5;

	/** 每秒速度值，依赖注入 */
	private int secondSpeed;
	/** 一秒被分割的份数，可注入修改，默认为5 */
	private int secondDivideNum = 5;

	/** 限速器运行标志 */
	private boolean runFlag = true;
	/** 速度计数器清零间隔时间 */
	private int clearIntervalMillis;
	/** 间隔时间计数上限 */
	private int intervalMillisCountMax;
	/** 速度计数器 */
	private AtomicInteger sendCount = new AtomicInteger(0);
	/** 计数的时间边界 */
	private AtomicLong time = new AtomicLong(System.currentTimeMillis());
	/** 计数同步锁 */
	private byte[] countLock = new byte[1];
	/** 计时同步锁 */
	private byte[] timeLock = new byte[1];
	/** 监控的瞬时速度 */
	private int monitorSpeed = 0;

	public void init() {
		// 速度计数器清零间隔时间初始化
		this.clearIntervalMillis = 1000 / secondDivideNum;
		// 间隔时间计数上限初始化
		this.intervalMillisCountMax = secondSpeed / secondDivideNum;
		// 启动限速计数清零线程
		new SpeedCountClearThread().start();
	}

	public void destroy() {
		this.runFlag = false;
		synchronized (countLock) {
			countLock.notify();
		}
		synchronized (timeLock) {
			timeLock.notify();
		}
	}

	@Override
	public void limitSpeed() {
		this.limitSpeed(1);
	}

	@Override
	public synchronized void limitSpeed(int size) {
		int needAddCount = size;
		while (runFlag && needAddCount > 0) {
			if (sendCount.get() >= intervalMillisCountMax) {
				synchronized (countLock) {
					try {
						countLock.wait(WAIT_MILLIS);
					} catch (InterruptedException e) {
					}
				}
			} else {
				int newCount = sendCount.addAndGet(needAddCount);
				needAddCount = newCount > intervalMillisCountMax ? newCount - intervalMillisCountMax : 0;
			}
		}
	}

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

	// 设置依赖注入方法end
	// ///////////////////////////////////////////////
	
	/**
	 * 速度计数清零线程类
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
					monitorSpeed = sendCount.getAndSet(0) * secondDivideNum;
					logger.debug("finish a speed count clear job!");
					synchronized (countLock) {
						countLock.notify();
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

}
