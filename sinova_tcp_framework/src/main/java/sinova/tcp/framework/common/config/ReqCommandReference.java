package sinova.tcp.framework.common.config;

import sinova.tcp.framework.action.IReqAction;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 请求类型的协议命令的关联内容类<br/>
 * @param <T1> 请求消息
 * @param <T2> 响应消息
 */
public class ReqCommandReference<T1 extends IReq, T2 extends IResp> extends CommandReferenceBase<T1> {

	/** 关联的请求处理action */
	private IReqAction<T1, T2> reqAction;

	public IReqAction<T1, T2> getReqAction() {
		return reqAction;
	}

	public void setReqAction(IReqAction<T1, T2> reqAction) {
		this.reqAction = reqAction;
	}

}
