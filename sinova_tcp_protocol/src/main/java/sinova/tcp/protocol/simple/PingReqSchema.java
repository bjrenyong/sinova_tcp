// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from proto

package sinova.tcp.protocol.simple;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;

import java.io.IOException;

import javax.annotation.Generated;

@Generated("java_bean_model")
public class PingReqSchema implements Schema<PingReq> {

	static final PingReq DEFAULT_INSTANCE = new PingReq();
	static final Schema<PingReq> SCHEMA = new PingReqSchema();

	public static PingReq getDefaultInstance() {
		return DEFAULT_INSTANCE;
	}

	public static Schema<PingReq> getSchema() {
		return SCHEMA;
	}

	public static final int FIELD_NONE = 0;

	public PingReqSchema() {
	}

	public PingReq newMessage() {
		return new PingReq();
	}

	public Class<PingReq> typeClass() {
		return PingReq.class;
	}

	public String messageName() {
		return PingReq.class.getSimpleName();
	}

	public String messageFullName() {
		return PingReq.class.getName();
	}

	public boolean isInitialized(PingReq message) {
		return true;
	}

	public void mergeFrom(Input input, PingReq message) throws IOException {
		for (int fieldIx = input.readFieldNumber(this); fieldIx != FIELD_NONE; fieldIx = input.readFieldNumber(this)) {
			mergeFrom(input, message, fieldIx);
		}
	}

	public void mergeFrom(Input input, PingReq message, int fieldIx) throws IOException {
		switch (fieldIx) {
		case FIELD_NONE:
			return;
		default:
			input.handleUnknownField(fieldIx, this);
		}
	}

	private static int[] FIELDS_TO_WRITE = {};

	public int[] getWriteFields() {
		return FIELDS_TO_WRITE;
	}

	public void writeTo(Output output, PingReq message) throws IOException {
		int[] toWrite = getWriteFields();
		for (int i = 0; i < toWrite.length; i++) {
			writeTo(output, message, toWrite[i]);
		}
	}

	public void writeTo(Output output, PingReq message, int fieldIx) throws IOException {
		switch (fieldIx) {
		case FIELD_NONE:
			break;
		default:
			break;
		}
	}

	public String getFieldName(int number) {
		return Integer.toString(number);
	}

	public int getFieldNumber(String name) {
		return Integer.parseInt(name);
	}
}
