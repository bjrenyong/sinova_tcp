package sinova.tcp.framework.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.common.codec.Protocol2MsgCodec;
import sinova.tcp.framework.common.codec.Req2TransportMsgEncoder;
import sinova.tcp.framework.common.codec.TransportProtocolDecoder;
import sinova.tcp.framework.common.codec.TransportProtocolEncoder;
import sinova.tcp.framework.common.handler.HeartbeatHandler;

/**
 * netty通信服务端
 * @author Timothy
 */
@Service
public class NettyServer {

	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

	/** 传输协议编码处理器 */
	private TransportProtocolEncoder transportEncoder = new TransportProtocolEncoder();
	/** 传输协议与消息对象间的编解码器，依赖注入 */
	@Autowired
	private Protocol2MsgCodec protocol2MsgCodec;
	@Autowired
	private Req2TransportMsgEncoder req2TransportMsgEncoder;
	/** 心跳处理器 */
	private HeartbeatHandler heartbeatHandler = new HeartbeatHandler();
	/** 其它I/O处理器，依赖注入 */
	@Resource
	private List<ChannelHandler> serverChannelHandlers;
	/** 服务端发起心跳标志，默认为发起，配置注入 */
	@Value("${netty.server.keepalive: true}")
	private boolean keepAlive = true;
	/** 心跳间隔，单位秒，小于等于0为不发送心跳，默认为30秒，配置注入 */
	@Value("${netty.server.readidle: 30}")
	private int readIdle = 30;
	@Value("${netty.server.bindPort: 8088}")
	private int bindPort = 8088;
	
	/////////////////////////////////////////////////////////
	// 暂未使用
	@Value("${netty.server.flushSize:100000}")
	private int flushSize;
	/** 多长时间写入内容到流，刷新频率 */
	@Value("${netty.server.flush.millsFlushRate: 5000}")
	private long millsFlush = 1000;
	/////////////////////////////////////////////////////////

	/** 主线程组，用于接受网络连接 */
	private EventLoopGroup bossGroup;
	/** 子线程组，用于处理网络读写 */
	private EventLoopGroup workerGroup;
	/** 服务端启动类实例 */
	private ServerBootstrap bootstrap;
	private ChannelFuture channelFuture;

	@PostConstruct
	public void doOpen() {
		// 用于接受网络连接的线程组初始化
		this.bossGroup = new NioEventLoopGroup();
		// 用于SocketChannel网络读写的线程组初始化
		this.workerGroup = new NioEventLoopGroup();
		// 初始化服务端启动类实例
		this.bootstrap = new ServerBootstrap();
		this.bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
		this.bootstrap.option(ChannelOption.SO_BACKLOG, 128).option(ChannelOption.SO_SNDBUF, flushSize)
				.option(ChannelOption.SO_RCVBUF, flushSize).childOption(ChannelOption.SO_KEEPALIVE, keepAlive);
		this.bootstrap.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(final SocketChannel socketChannel) throws Exception {
				ChannelPipeline p = socketChannel.pipeline();
				// 设置心跳触发handler，这个必须放在前面，否则可能会失效
				// 最后一个参数不能为0 设置为0后导致心跳不发送
				p.addLast(new IdleStateHandler(readIdle * 3, readIdle, readIdle));
				// 添加编码处理
				p.addLast(transportEncoder);
				// 添加解码处理
				p.addLast(new TransportProtocolDecoder());
				// 添加传输协议与传输消息间的编解码器
				p.addLast(protocol2MsgCodec);
				// 添加请求消息到传输消息间的编码处理
				p.addLast(req2TransportMsgEncoder);
				// 如果有其他处理将handler依次放入
				if (serverChannelHandlers != null) {
					for (ChannelHandler channelHandler : serverChannelHandlers) {
						p.addLast(channelHandler);
					}
				}
				if (keepAlive) {
					// 设置心跳处理
					p.addLast(heartbeatHandler);
				}
				// socketChannel.eventLoop().scheduleWithFixedDelay(new Runnable() {
				// @Override
				// public void run() {
				// socketChannel.flush();
				// }
				// }, millsFlush, millsFlush, TimeUnit.MILLISECONDS);
			}
		});
		try {
			channelFuture = this.bootstrap.bind(bindPort).sync();
			if (channelFuture.isSuccess()) {
				logger.info("server start success");
			} else {
				logger.error("server start fail");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@PreDestroy
	public void doClose() {
		// TODO: 有待验证
		channelFuture.channel().close();
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();

		// channelFuture.channel().close();
		// channelFuture.channel().parent().close();
	}

}
