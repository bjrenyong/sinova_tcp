package sinova.tcp.framework.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.framework.common.protocol.TransportProtocol;

/**
 * 传输协议编码器-炎黄标准版本<br/>
 * 炎黄标准版本中，协议格式对应于TransportProtocol类：<br/>
 * 1. 4位数字标识协议消息总长度<br/>
 * 2. 2位数字标识协议消息类型<br/>
 * 3. 4位数字标识协议消息传输用的流水号<br/>
 * 4. 协议消息体
 *
 * @author Timothy
 */
@Sharable
public class TransportProtocolEncoder extends MessageToByteEncoder<TransportProtocol> {
    private static final Logger logger = LoggerFactory.getLogger(TransportProtocolEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, TransportProtocol msg, ByteBuf out) throws Exception {
        try {
            validate(msg);
            // 写入消息总长度值（其中消息头有固定的10位长度，包括长度字段自身）
            if (msg.getTransportBody() != null) {
                out.writeInt(msg.getTransportBody().length + 10);
                out.writeShort(msg.getCommandId());
                out.writeInt(msg.getSequenceId());
                out.writeBytes(msg.getTransportBody());
            } else {
                out.writeInt(10);
                out.writeShort(msg.getCommandId());
                out.writeInt(msg.getSequenceId());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            try {
                if (out != null) {
                    out.release();
                }
            } catch (Exception e) {

            }
            ctx.close();
        }

    }

    private void validate(TransportProtocol msg) throws Exception {
        if (msg == null) {
            throw new Exception("The encode message is null");
        }
    }
}
