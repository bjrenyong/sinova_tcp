// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from proto

package sinova.tcp.protocol.simple;

import java.util.Objects;

import sinova.tcp.protocol.IErrorResp;

public class ErrorResp implements IErrorResp {

	String errorCode;
	String errorMsg;

	public ErrorResp() {
	}

	public ErrorResp(String errorCode, String errorMsg) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}

	public String getErrorCode() {

		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {

		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final ErrorResp that = (ErrorResp) obj;
		return Objects.equals(this.errorCode, that.errorCode) && Objects.equals(this.errorMsg, that.errorMsg);
	}

	@Override
	public int hashCode() {
		return Objects.hash(errorCode, errorMsg);
	}

	@Override
	public String toString() {
		return "ErrorResp{" + "errorCode=" + errorCode + ", errorMsg=" + errorMsg + '}';
	}

}
