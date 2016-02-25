package sinova.tcp.framework.common.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sinova.tcp.protocol.IPingReq;
import sinova.tcp.protocol.simple.PingReq;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 心跳处理类，配合IdleStateHandler使用<br/>
 * @author Timothy
 */
@Sharable
public class HeartbeatHandler extends ChannelHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

	private IPingReq pingReq = new PingReq();

	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			switch (event.state()) {
			case READER_IDLE:
				logger.info(ctx.channel() + " time out, connection close!");
				ctx.close();
			case WRITER_IDLE:
				// 发起心跳请求
				logger.info(ctx.channel() + "idle, send ping request!");
				ctx.writeAndFlush(pingReq);
			default:
				break;
			}
		}
		super.userEventTriggered(ctx, evt);
	}

}
