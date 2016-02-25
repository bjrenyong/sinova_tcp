// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from proto

package sinova.tcp.protocol.standard.demo;

import java.util.Objects;

import sinova.tcp.protocol.IReq;

public class SmsMtReq implements IReq {

	ResendInfo resendInfo;
	Long clientMtSequence;
	String mobileNum;
	String smsContent;
	Integer priority;

	public SmsMtReq() {
	}

	public SmsMtReq(Long clientMtSequence, String mobileNum, String smsContent, Integer priority) {
		this.clientMtSequence = clientMtSequence;
		this.mobileNum = mobileNum;
		this.smsContent = smsContent;
		this.priority = priority;
	}

	public ResendInfo getResendInfo() {

		return resendInfo;
	}

	public void setResendInfo(ResendInfo resendInfo) {
		this.resendInfo = resendInfo;
	}

	public Long getClientMtSequence() {

		return clientMtSequence;
	}

	public void setClientMtSequence(Long clientMtSequence) {
		this.clientMtSequence = clientMtSequence;
	}

	public String getMobileNum() {

		return mobileNum;
	}

	public void setMobileNum(String mobileNum) {
		this.mobileNum = mobileNum;
	}

	public String getSmsContent() {

		return smsContent;
	}

	public void setSmsContent(String smsContent) {
		this.smsContent = smsContent;
	}

	public Integer getPriority() {

		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final SmsMtReq that = (SmsMtReq) obj;
		return Objects.equals(this.resendInfo, that.resendInfo)
				&& Objects.equals(this.clientMtSequence, that.clientMtSequence)
				&& Objects.equals(this.mobileNum, that.mobileNum) && Objects.equals(this.smsContent, that.smsContent)
				&& Objects.equals(this.priority, that.priority);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resendInfo, clientMtSequence, mobileNum, smsContent, priority);
	}

	@Override
	public String toString() {
		return "SmsMtReq{" + "resendInfo=" + resendInfo + ", clientMtSequence=" + clientMtSequence + ", mobileNum="
				+ mobileNum + ", smsContent=" + smsContent + ", priority=" + priority + '}';
	}
}
