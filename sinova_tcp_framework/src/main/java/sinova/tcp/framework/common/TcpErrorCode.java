package sinova.tcp.framework.common;

/**
 * TCP错误码
 * @author Timothy
 */
public enum TcpErrorCode {

	// -1到-50，可重发的异常
	/** 系统异常 */
	ERRORCODE_SYSTEM_EXCEPTION("-1", "system exception"),
	/** 通道连接异常 */
	ERRORCODE_TCP_CONNECTION("-2", "tcp connection exception"),
	/** 速度超流控 */
	ERRORCODE_SPEED_OVERFLOW("-3", "speed overflow"),
	/** 消息发送超时 */
	ERRORCODE_TIMEOUT("-4", "send message timeout"),
	// -51到-99，不可重发的异常
	/** 消息格式错误 */
	ERRORCODE_MESSAGE_FORMAT("-51", "message format error"),
	/** 连接类型不允许发送消息到客户端 */
	ERRORCODE_NOTALLOW_MO("-52", "connection type not allow send message to client"),
	/** 连接类型不允许发送消息到服务端 */
	ERRORCODE_NOTALLOW_MT("-53", "connection type not allow send message to server");

	private String errorCode;
	private String errorMsg;

	private TcpErrorCode(String errorCode, String errorMsg) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

}
