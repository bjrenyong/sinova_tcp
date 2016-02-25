package sinova.tcp.framework.common.protocol;

import java.io.Serializable;

/**
 * 传输协议类<br/>
 * 专用于client和server间的数据传输，具体承载的消息在transportBody中
 * @author Timothy
 */
public class TransportProtocol implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 协议消息总长度(含消息头及消息体) */
	private int totalLen;
	/** 协议消息类型 */
	private short commandId;
	/** 协议消息流水号,顺序累加,步长为1,循环使用（一对请求和应答消息的流水号必须相同） */
	private int sequenceId;
	/** 协议消息体 */
	private byte[] transportBody;

	public int getTotalLen() {
		return totalLen;
	}

	public void setTotalLen(int totalLen) {
		this.totalLen = totalLen;
	}

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

	public byte[] getTransportBody() {
		return transportBody;
	}

	public void setTransportBody(byte[] transportBody) {
		this.transportBody = transportBody;
	}

	/**
	 * 计算传输协议自身的长度并赋值给totalLen
	 */
	public void calculateTotalLen() {
		this.totalLen = transportBody.length + 10;
	}
}
