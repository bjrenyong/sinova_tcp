package sinova.tcp.framework.common.monitor;

import java.util.Map;

/**
 * 缺省可以统计清0的IMonitor接口，返回的监控结果是Map
 * @author Timothy
 */
public interface IDefaultClearMonitor extends IDefaultMonitor, IClearMonitor<Map<String, Object>> {

}
