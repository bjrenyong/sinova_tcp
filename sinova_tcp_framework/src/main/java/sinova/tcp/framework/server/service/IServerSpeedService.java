package sinova.tcp.framework.server.service;

import sinova.tcp.framework.common.monitor.IOverFalseSpeed;
import sinova.tcp.framework.common.monitor.IOverWaitSpeed;

/**
 * 服务端限速服务接口
 * @author Timothy
 */
public interface IServerSpeedService {

	/**
	 * 获取某用户的服务端业务请求发送限速器
	 * @param userId 用户ID
	 * @return 用户的服务端业务请求发送限速器
	 */
	public IOverWaitSpeed getSendSpeedByUserId(Integer userId);
	
	/**
	 * 获取某用户的服务端业务请求接收限速器
	 * @param userId 用户ID
	 * @return 用户的服务端业务请求接收限速器
	 */
	public IOverFalseSpeed getReceiveSpeedByUserId(Integer userId);
}
