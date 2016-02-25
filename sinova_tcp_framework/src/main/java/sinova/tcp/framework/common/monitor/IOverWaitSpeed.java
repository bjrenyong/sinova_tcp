package sinova.tcp.framework.common.monitor;

/**
 * 超速等待限速接口-对于速度超过规定限制的进行延时等待<br>
 * 一般TCP请求端适合使用超速等待限速类进行限速，而接收者适合使用超速等待限速类进行限速
 * @author Timothy
 */
public interface IOverWaitSpeed {

	/**
	 * 限速计数+1，超限的进行延时等待
	 */
	public void limitSpeed();

	/**
	 * 限速计数+size，超限的进行延时等待
	 * @param size 计数步长
	 */
	public void limitSpeed(int size);

	/** 
	 * 获取当前的监控速度值 
	 */
	public int getMonitorSpeed();
}
