package sinova.tcp.framework.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.common.protocol.TransportProtocol;

/**
 * 传输协议解码器-炎黄标准版本<br/>
 * 炎黄标准版本中，协议格式对应于TransportProtocol类：<br/>
 * 1. 4位数字标识协议消息总长度<br/>
 * 2. 2位数字标识协议消息类型<br/>
 * 3. 4位数字标识协议消息传输用的流水号<br/>
 * 4. 协议消息体
 * @author Timothy
 */
public class TransportProtocolDecoder extends LengthFieldBasedFrameDecoder {
	private static final Logger logger = LoggerFactory.getLogger(TransportProtocolDecoder.class);

	/**
	 * 协议消息允许的最大长度
	 */
	private static int msgAllowMaxLength = 1024 * 1024 * 100;

	public TransportProtocolDecoder() {
		// 消息长度字段从0开始，占4字节，消息总长度包括4字节的消息长度字段
		super(msgAllowMaxLength, 0, 4, -4, 0);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		ByteBuf frame = null;
		try {
			frame = (ByteBuf) super.decode(ctx, in);
			if (frame == null) {
				return null;
			}
			TransportProtocol protocol = new TransportProtocol();
			protocol.setTotalLen(frame.readInt());
			protocol.setCommandId(frame.readShort());
			protocol.setSequenceId(frame.readInt());
			byte[] msgBody = new byte[protocol.getTotalLen() - 10];
			frame.readBytes(msgBody);
			protocol.setTransportBody(msgBody);
			return protocol;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			ctx.close();
			return null;
		} finally {
			try {
				ReferenceCountUtil.release(frame);
			} catch (Exception e) {
			}
		}
	}

}
