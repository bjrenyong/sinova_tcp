package sinova.tcp.framework.common.monitor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

/**
 * 监控线程基类，抽象类<br>
 * 子类需实现的方法：<br>
 * 1. init--线程初始化<br>
 * 2. clear--监控数据初始化<br>
 * 3. doOneMonitor--执行一次监控操作<br>
 * 4. destroy--线程结束销毁工作<br>
 * 5. getMonitorResult--取得监控结果<br>
 * 6. getJsonResult--取得Json格式的监控结果
 * @author Timothy
 */
@Service
public abstract class BaseMonitorRunnable implements IBaseMoinitorRunnable {

	/** 监控间隔时间，单位毫秒，依赖注入 */
	private int monitorWaitTime = 1000;

	/** 线程运行标志 */
	private boolean runFlag = true;
	/** 线程锁 */
	private byte[] lock = new byte[1];

	@Override
	public void run() {
		// 线程初始化
		init();
		// 监控数据初始化
		clear();
		// 循环
		while (runFlag) {
			// 执行一次循环操作
			doOneMonitor();
			doSleep(monitorWaitTime);
		}
		// 执行销毁操作
		destroy();
	}

	/**
	 * 执行休眠，单位毫秒
	 * @param sleepTime 休眠时间，单位毫秒
	 */
	public void doSleep(long sleepTime) {
		try {
			if (sleepTime > 0) {
				synchronized (lock) {
					lock.wait(sleepTime);
				}
			}
		} catch (InterruptedException e) {
		}
	}

	@PostConstruct
	@Override
	public void start() {
		new Thread(this).start();
	}

	@PreDestroy
	@Override
	public void stop() {
		this.runFlag = false;
		lock.notify();
	}

	@Override
	public void setMonitorWaitTime(int monitorWaitTime) {
		this.monitorWaitTime = monitorWaitTime;
	}

	public int getMonitorWaitTime() {
		return monitorWaitTime;
	}
}
