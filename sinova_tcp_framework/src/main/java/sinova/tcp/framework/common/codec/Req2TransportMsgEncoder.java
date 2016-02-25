package sinova.tcp.framework.common.codec;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.common.service.TransportMsgService;
import sinova.tcp.protocol.IReq;

/**
 * 请求类消息到传输消息的编码处理器
 *
 * @author Timothy
 */
@Service
@Sharable
public class Req2TransportMsgEncoder extends MessageToMessageEncoder<IReq> {

    private static final Logger logger = LoggerFactory.getLogger(Req2TransportMsgEncoder.class);

    @Autowired
    private TransportMsgService transportMsgService;

    @Override
    protected void encode(ChannelHandlerContext ctx, IReq msg, List<Object> out) throws Exception {
        try {
            logger.debug("Req2TransportMsg encode begin, req=" + msg);
            TransportMsg transportMsg = transportMsgService.req2TransportMsg(msg);
            logger.debug("Req2TransportMsg encode success, commandId=" + transportMsg.getCommandId() + ", sequenceId="
                    + transportMsg.getSequenceId());
            out.add(transportMsg);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            ctx.close();
        }
    }

}
