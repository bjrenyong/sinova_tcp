package sinova.tcp.framework.common.exception;

import sinova.tcp.framework.common.TcpErrorCode;
import sinova.tcp.protocol.simple.ErrorResp;

/**
 * Created by haoxiaodong on 2016-1-15.<br/>
 * 业务处理异常，在业务发送和接收过程中使用
 */
public class BusinessException extends Exception {

	private static final long serialVersionUID = 6803947647035212260L;
	/** 错误码 */
	private String errorCode;
	/** 错误消息 */
	private String errorMsg;

	public BusinessException(String errorCode, String errorMsg) {
		super("code:" + errorCode + ", message:" + errorMsg);
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}

	public BusinessException(ErrorResp errorResp) {
		this(errorResp.getErrorCode(), errorResp.getErrorMsg());
	}

	public BusinessException(TcpErrorCode tcpErrorCode) {
		this(tcpErrorCode.getErrorCode(), tcpErrorCode.getErrorMsg());
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
}
