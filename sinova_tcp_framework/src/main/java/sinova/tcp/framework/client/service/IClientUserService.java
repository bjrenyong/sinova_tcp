package sinova.tcp.framework.client.service;

import sinova.tcp.framework.common.monitor.TcpSpeedRecord;
import sinova.tcp.protocol.ILoginReq;

/**
 * Created by haoxiaodong on 2016-1-15.<br/>
 * client端用户服务接口
 */
public interface IClientUserService {

	/**
	 * 创建登录请求消息
	 * @return 登录请求消息
	 */
	public ILoginReq createLoginReq();

	/**
	 * 获取客户端用户ID
	 * @return 客户端用户ID
	 */
	public int getUserId();

	/**
	 * 获取客户端速度计数器
	 * @return 客户端速度计数器
	 */
	public TcpSpeedRecord getClientSpeedRecord();
}
