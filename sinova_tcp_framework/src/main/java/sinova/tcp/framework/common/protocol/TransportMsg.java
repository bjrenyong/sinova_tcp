package sinova.tcp.framework.common.protocol;

import sinova.tcp.protocol.IMsg;

/**
 * 传输消息
 * @author Timothy
 */
public class TransportMsg {

	/** 协议消息类型 */
	private short commandId;
	/** 协议消息流水号,顺序累加,步长为1,循环使用（一对请求和应答消息的流水号必须相同） */
	private int sequenceId;
	/** 消息 */
	private IMsg msg;

	public short getCommandId() {
		return commandId;
	}

	public void setCommandId(short commandId) {
		this.commandId = commandId;
	}

	public int getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}

	public IMsg getMsg() {
		return msg;
	}

	public void setMsg(IMsg msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "TransportMsg{" + "commandId=" + commandId + ", sequenceId=" + sequenceId + ", msg=" + msg + "}";
	}
}
