package sinova.tcp.framework.common.monitor;

/**
 * 监控接口
 * @author Timothy
 * @param <T> 监控结果
 */
public interface IMonitor<T> {

	/**
	 * 取得监控结果
	 * @return 监控结果
	 */
	public T getMonitorResult();

}