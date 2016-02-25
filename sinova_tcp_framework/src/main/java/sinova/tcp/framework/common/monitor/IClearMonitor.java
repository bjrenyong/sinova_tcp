package sinova.tcp.framework.common.monitor;

/**
 * 可进行统计清0操作的监控接口
 * @author Timothy
 * @param <T> 监控结果
 */
public interface IClearMonitor<T> extends IMonitor<T> {

	/**
	 * 统计数据清0
	 */
	public void clear();
}
