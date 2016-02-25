package sinova.tcp.framework.common.monitor;

/**
 * 超速失败限速接口-对于速度超过规定限制的返回失败信息<br/>
 * 一般TCP请求端适合使用超速等待限速类进行限速，而接收者适合使用超速等待限速类进行限速
 * @author Timothy
 */
public interface IOverFalseSpeed {

	/**
	 * 限速计数+1，超限的返回false
	 * @return 是否符合限速要求，false:超限; true:正常
	 */
	public boolean limitSpeed();

	/**
	 * 获取当前的监控速度值
	 */
	public int getMonitorSpeed();

}
