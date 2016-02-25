package sinova.tcp.framework.common.protostuff.codec;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.common.codec.Protocol2MsgCodec;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.common.protocol.TransportProtocol;
import sinova.tcp.framework.common.protostuff.service.ProtoCommandReferenceService;
import sinova.tcp.protocol.IMsg;

/**
 * 传输协议和传输消息之间的编码转换--基于Protostuff实现
 * @author Timothy
 */
@Service
@Sharable
public class ProtostuffProtocol2MsgCodec extends Protocol2MsgCodec {

	private static final Logger logger = LoggerFactory.getLogger(ProtostuffProtocol2MsgCodec.class);

	/** 命令关联信息服务 */
	@Autowired
	private ProtoCommandReferenceService commandReferenceService;

	/**
	 * 检查消息本身是否符合编码要求
	 * @param msg 待检查的消息
	 * @throws Exception
	 */
	private void validateMsg4Encode(IMsg msg) throws Exception {
		Schema<? extends IMsg> schema = commandReferenceService.getClass2SchemaMap().get(msg.getClass());
		if (schema == null) {
			// 命令的编码器映射不存在
			throw new Exception("command's encoder not exist! commandClass=" + msg.getClass().getName());
		} else if (commandReferenceService.getCommandIdByMsgClass(msg.getClass()) == null) {
			// 命令的命令ID不存在
			throw new Exception("command's commandId not exist! commandClass=" + msg.getClass().getName());
		}
	}

	/**
	 * 检查协议本身是否符合解码要求
	 * @param protocol 传输协议
	 * @throws Exception
	 */
	private void validateProtocol4Decode(TransportProtocol protocol) throws Exception {
		Schema<? extends IMsg> schema = commandReferenceService.getCommandId2SchemaMap().get(protocol.getCommandId());
		if (schema == null) {
			// 传输协议消息体的解码器不存在
			throw new Exception("TransportProtocol's decoder not exist! commandId=" + protocol.getCommandId());
		} else if (commandReferenceService.getMsgClassByCommandId(protocol.getCommandId()) == null) {
			// 命令ID对应的命令不存在
			throw new Exception("commandId's command not exiist! commandId=" + protocol.getCommandId());
		}
	}

	/**
	 * 编码生成协议消息体
	 * @param msg 消息
	 * @return 协议消息体
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private byte[] encode2TransportBody(IMsg msg) {
		// 获取对应的schema
		Schema schema = commandReferenceService.getClass2SchemaMap().get(msg.getClass());
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		byte[] data = ProtobufIOUtil.toByteArray(msg, schema, buffer);
		buffer.clear();
		return data;
	}

	/**
	 * 传输消息编码为传输协议
	 * @param ctx ChannelHandlerContext
	 * @param transportMsg 传输消息
	 * @param out List<Object>
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, TransportMsg transportMsg, List<Object> out) throws Exception {
		IMsg msg = transportMsg.getMsg();
		validateMsg4Encode(msg);
		byte[] transportBody = encode2TransportBody(msg);
		TransportProtocol protocol = new TransportProtocol();
		protocol.setCommandId(commandReferenceService.getCommandIdByMsgClass(msg.getClass()));
		protocol.setSequenceId(transportMsg.getSequenceId());
		protocol.setTransportBody(transportBody);
		// 计算传输协议自身的长度并赋值给totalLen
		protocol.calculateTotalLen();
		out.add(protocol);
		if (logger.isDebugEnabled()) {
			logger.debug("TransportMsg to TransportProtocol encode success! transportMsg=" + transportMsg);
		}
	}

	/**
	 * 传输协议解码为传输消息
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void decode(ChannelHandlerContext ctx, TransportProtocol protocol, List<Object> out) throws Exception {
		validateProtocol4Decode(protocol);
		Schema schema = commandReferenceService.getCommandId2SchemaMap().get(protocol.getCommandId());
		IMsg message = commandReferenceService.getMsgClassByCommandId(protocol.getCommandId()).newInstance();
		ProtobufIOUtil.mergeFrom(protocol.getTransportBody(), message, schema);
		TransportMsg transportMsg = new TransportMsg();
		transportMsg.setCommandId(protocol.getCommandId());
		transportMsg.setSequenceId(protocol.getSequenceId());
		transportMsg.setMsg(message);
		out.add(transportMsg);
		if (logger.isDebugEnabled()) {
			logger.debug("TransportProtocol to TransportMsg decode success! transportMsg=" + transportMsg);
		}
	}
}
