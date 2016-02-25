package sinova.tcp.framework.common.protostuff.config;

import io.protostuff.Schema;
import sinova.tcp.framework.common.config.ReqCommandReference;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 请求类型的协议命令的关联内容类-基于protostuff
 * @author Timothy
 * @param <T1> 请求消息
 * @param <T2> 响应消息
 */
public class ProtoReqCommandReference<T1 extends IReq, T2 extends IResp> extends ReqCommandReference<T1, T2> {

	/** 请求消息对应的schema */
	private Schema<T1> reqSchema;

	public Schema<T1> getReqSchema() {
		return reqSchema;
	}

	public void setReqSchema(Schema<T1> reqSchema) {
		this.reqSchema = reqSchema;
	}

}
