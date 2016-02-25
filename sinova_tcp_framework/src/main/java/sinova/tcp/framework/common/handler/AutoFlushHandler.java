package sinova.tcp.framework.common.handler;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class AutoFlushHandler extends ChannelHandlerAdapter {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		// if(ctx.channel().unsafe())
		if (ctx.channel().unsafe().outboundBuffer().size() > 4096) {
			this.flush(ctx);
		} else {
			super.write(ctx, msg, promise);
		}
	}

}
