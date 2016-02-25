package sinova.tcp.framework.common.service;

import sinova.tcp.framework.action.IReqAction;
import sinova.tcp.framework.action.IRespAction;
import sinova.tcp.protocol.IMsg;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 命令关系信息服务接口
 * @author Timothy
 */
public interface ICommandReferenceService {

	/**
	 * 根据命令ID获取消息类类型
	 * @param commandId 命令ID
	 * @return 消息类类型
	 */
	public abstract Class<? extends IMsg> getMsgClassByCommandId(Short commandId);

	/**
	 * 根据消息类类型获取命令ID
	 * @param msgClass 消息类类型
	 * @return 命令ID
	 */
	public abstract Short getCommandIdByMsgClass(Class<? extends IMsg> msgClass);

	/**
	 * 根据请求命令类类型获取请求消息处理实例
	 * @param reqClass 请求命令类类型
	 * @return 请求消息处理实例
	 */
	public abstract IReqAction<? extends IReq, ? extends IResp> getReqActionByReqClass(Class<? extends IReq> reqClass);

	/**
	 * 判断请求消息是否有对应的关联信息
	 * @param reqMsg 请求消息
	 * @return 是否有对应的关联信息
	 */
	public abstract boolean reqHasReference(IReq reqMsg);

	/**
	 * 根据响应命令类类型获取响应消息处理实例
	 * @param respClass 响应命令类类型
	 * @return 响应消息处理实例
	 */
	public abstract IRespAction<? extends IReq, ? extends IResp> getRespActionByRespClass(
			Class<? extends IResp> respClass);

	/**
	 * 根据响应命令类类型获取响应命令ID
	 * @param respClass 响应命令类类型
	 * @return 响应命令ID
	 */
	public abstract Short getRespCommandIdByRespClass(Class<? extends IResp> respClass);

}