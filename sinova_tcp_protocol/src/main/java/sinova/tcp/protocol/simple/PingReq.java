// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from proto

package sinova.tcp.protocol.simple;

import sinova.tcp.protocol.IPingReq;

public class PingReq implements IPingReq {

	public PingReq() {
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final PingReq that = (PingReq) obj;
		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public String toString() {
		return "PingReq{" + '}';
	}
}
