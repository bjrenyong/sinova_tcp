// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from proto

package sinova.tcp.protocol.standard.demo;

import java.util.Objects;

import sinova.tcp.protocol.IResp;

public class SmsReportResp implements IResp {

	Long clientMtSequence;
	Long sysMtSequence;

	public SmsReportResp() {
	}

	public SmsReportResp(Long clientMtSequence, Long sysMtSequence) {
		this.clientMtSequence = clientMtSequence;
		this.sysMtSequence = sysMtSequence;
	}

	public Long getClientMtSequence() {

		return clientMtSequence;
	}

	public void setClientMtSequence(Long clientMtSequence) {
		this.clientMtSequence = clientMtSequence;
	}

	public Long getSysMtSequence() {

		return sysMtSequence;
	}

	public void setSysMtSequence(Long sysMtSequence) {
		this.sysMtSequence = sysMtSequence;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final SmsReportResp that = (SmsReportResp) obj;
		return Objects.equals(this.clientMtSequence, that.clientMtSequence)
				&& Objects.equals(this.sysMtSequence, that.sysMtSequence);
	}

	@Override
	public int hashCode() {
		return Objects.hash(clientMtSequence, sysMtSequence);
	}

	@Override
	public String toString() {
		return "SmsReportResp{" + "clientMtSequence=" + clientMtSequence + ", sysMtSequence=" + sysMtSequence + '}';
	}
}