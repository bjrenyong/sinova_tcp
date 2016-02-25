package sinova.tcp.framework.common.codec;

import io.netty.handler.codec.MessageToMessageCodec;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.common.protocol.TransportProtocol;

/**
 * 传输协议和传输消息之间的编码转换<br/>
 * 1. 传输协议解码后生成传输消息<br/>
 * 2. 传输消息编码后生成传输协议
 * @author Timothy
 */
public abstract class Protocol2MsgCodec extends MessageToMessageCodec<TransportProtocol, TransportMsg> {

}
