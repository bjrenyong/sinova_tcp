package sinova.tcp.framework.action;

import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 响应消息处理接口
 * @param <T1> 请求消息
 * @param <T2> 响应消息
 */
public interface IRespAction<T1 extends IReq, T2 extends IResp> {

	/**
	 * 执行响应操作
	 * @param request 请求消息，可以是null
	 * @param response 响应消息
	 */
	public void action(T1 request, T2 response);

}
