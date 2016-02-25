package sinova.tcp.framework.common.protostuff.config;

import io.protostuff.Schema;
import sinova.tcp.framework.common.config.RespCommandReference;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 响应类型的协议命令的关联内容类--基于protostuff实现
 * @author Timothy
 * @param <T1> 请求消息
 * @param <T2> 响应消息
 */
public class ProtoRespCommandReference<T1 extends IReq, T2 extends IResp> extends RespCommandReference<T1, T2> {

	/** 响应消息对应的schema */
	private Schema<T2> respSchema;

	public Schema<T2> getRespSchema() {
		return respSchema;
	}

	public void setRespSchema(Schema<T2> respSchema) {
		this.respSchema = respSchema;
	}

}
