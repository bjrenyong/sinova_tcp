package sinova.tcp.framework.common.config;

import sinova.tcp.framework.action.IRespAction;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 响应类型的协议命令的关联内容类<br/>
 * @param <T1> 请求消息
 * @param <T2> 响应消息
 */
public class RespCommandReference<T1 extends IReq, T2 extends IResp> extends CommandReferenceBase<T2> {

	/** 关联的响应处理action */
	private IRespAction<T1, T2> respAction;

	public IRespAction<T1, T2> getRespAction() {
		return respAction;
	}

	public void setRespAction(IRespAction<T1, T2> respAction) {
		this.respAction = respAction;
	}

}
