package sinova.tcp.framework.common.config;

import sinova.tcp.protocol.IMsg;

/**
 * 协议命令的关联信息基类<br/>
 * 用于存储某命令对应的关联信息<br/>
 * @author Timothy
 * @param <T> IMsg的子类
 */
public abstract class CommandReferenceBase<T extends IMsg> {

	/** 命令ID */
	private short commandId;
	/** 命令对应的消息类 */
	private Class<T> commandClass;

	public short getCommandId() {
		return commandId;
	}

	public void setCommandId(short commandId) {
		this.commandId = commandId;
	}

	public Class<T> getCommandClass() {
		return commandClass;
	}

	public void setCommandClass(Class<T> commandClass) {
		this.commandClass = commandClass;
	}

}
