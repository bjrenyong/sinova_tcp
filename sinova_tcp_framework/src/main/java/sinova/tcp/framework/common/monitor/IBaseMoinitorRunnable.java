package sinova.tcp.framework.common.monitor;

/**
 * 基础的监控线程类接口<br>
 * 继承自IDefaultClearMonitor：getMonitorResult,clear<br>
 * 继承自Runnable: run
 * @author Timothy
 */
public interface IBaseMoinitorRunnable extends IDefaultClearMonitor, Runnable{

	/**
	 * 线程初始化
	 */
	public void init();
	
	/**
	 * 执行休眠，单位毫秒
	 * @param sleepTime 休眠时间，单位毫秒
	 */
	public void doSleep(long sleepTime);

	/**
	 * 执行一次监控操作
	 */
	public void doOneMonitor();
	
	/**
	 * 线程结束销毁工作
	 */
	public void destroy();

	/**
	 * 启动线程
	 */
	public void start();
	
	/**
	 * 结束线程
	 */
	public void stop();
	
	/**
	 * 设定监控等待时间，单位毫秒，依赖注入使用
	 * @param monitorWaitTime 监控等待时间，单位毫秒
	 */
	public void setMonitorWaitTime(int monitorWaitTime);
}
