package sinova.tcp.framework.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.client.service.IClientUserService;
import sinova.tcp.framework.common.codec.Protocol2MsgCodec;
import sinova.tcp.framework.common.codec.Req2TransportMsgEncoder;
import sinova.tcp.framework.common.codec.TransportProtocolDecoder;
import sinova.tcp.framework.common.codec.TransportProtocolEncoder;
import sinova.tcp.framework.common.handler.HeartbeatHandler;
import sinova.tcp.protocol.ILoginReq;
import sinova.tcp.protocol.ILogoutReq;
import sinova.tcp.protocol.simple.LogoutReq;

/**
 * netty通信客户端
 * @author Timothy
 */
@Service
public class NettyClient {

	private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

	/** 传输协议编码处理器，Sharable */
	private TransportProtocolEncoder transportEncoder = new TransportProtocolEncoder();
	/** 传输协议与消息对象间的编解码器，依赖注入 */
	@Autowired
	private Protocol2MsgCodec protocol2MsgCodec;
	/** 服务端发起心跳标志，默认为不发起，配置注入 */
	@Value("${netty.server.keepalive: false}")
	private boolean keepAlive = false;
	/** 请求消息到传输协议的编码转换器，依赖注入 */
	@Autowired
	private Req2TransportMsgEncoder req2TransportMsgEncoder;
	/** 心跳处理器，Sharable */
	private HeartbeatHandler heartbeatHandler = new HeartbeatHandler();
	/** 其它I/O处理器，依赖注入 */
	@Resource
	private List<ChannelHandler> clientChannelHandlers;

	/** 心跳间隔，单位秒，小于等于0为不发送心跳，默认为30秒，配置注入 */
	@Value("${netty.client.readidle:30}")
	private int readIdle = 0;
	@Value("${netty.server.ip:127.0.0.1}")
	private String serverIP;
	@Value("${netty.sever.port:8088}")
	private int serverPort;
	@Value("${netty.client.millsFlushRate:5000}")
	private long millsFlushRate = 1000;
	@Value("${netty.client.flushSize:261244}")
	private int flushSize;
	private Bootstrap bootstrap = new Bootstrap();
	/** 登录用户信息 */
	@Autowired
	private IClientUserService clientUserService;

	@PostConstruct
	public void init() {
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		bootstrap.group(workerGroup).channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.SO_SNDBUF, flushSize)
				.option(ChannelOption.SO_RCVBUF, flushSize);
		bootstrap.handler(new LoggingHandler(LogLevel.DEBUG));
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(final SocketChannel socketChannel) throws Exception {
				ChannelPipeline p = socketChannel.pipeline();
				// 设置心跳触发handler，这个必须放在前面，否则可能会失效
				// 最后一个参数不能为0 设置为0后导致心跳不发送
				p.addLast(new IdleStateHandler(readIdle * 3, readIdle, readIdle));
				// 添加编码处理
				p.addLast(transportEncoder);
				// 添加解码处理(非sharable，所以必须在这里构造)
				p.addLast(new TransportProtocolDecoder());
				// 添加传输协议与传输消息间的编解码器
				p.addLast(protocol2MsgCodec);
				// 添加请求消息到传输消息间的编码处理
				p.addLast(req2TransportMsgEncoder);
				// 如果有其他处理将handler依次放入
				if (clientChannelHandlers != null) {
					for (ChannelHandler channelHandler : clientChannelHandlers) {
						p.addLast(channelHandler);
					}
				}
				if (keepAlive) {
					// 设置心跳处理
					p.addLast(heartbeatHandler);
				}
				// 定时flush数据
				// socketChannel.eventLoop().scheduleWithFixedDelay(new Runnable() {
				// @Override
				// public void run() {
				// socketChannel.flush();
				// }
				//
				// }, millsFlushRate, millsFlushRate, TimeUnit.MILLISECONDS);
			}
		});
	}

	/**
	 * 执行netty连接和登录操作
	 */
	public void connect() {
		try {
			ChannelFuture future = bootstrap.connect(serverIP, serverPort).sync();
			if (future.isSuccess()) {
				logger.info("connect to server success! channel=" + future.channel());
				ILoginReq loginReq = clientUserService.createLoginReq();
				future.channel().writeAndFlush(loginReq);
			} else {
				logger.info("connect to server failed! channel=" + future.channel());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 执行登出和断开连接 操作
	 * @param channel Channel
	 */
	public void disconnect(Channel channel) {
		if (channel != null) {
			if (channel.isActive()) {
				// 发起登出请求
				logger.info("send logout request to server! channel=" + channel);
				ILogoutReq logoutReq = createLogoutReq();
				try {
					channel.writeAndFlush(logoutReq).sync();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				logger.info("the channel is not active, close directly! channel=" + channel);
			}
			channel.close();
		}
	}

	/**
	 * 创建登出请求消息
	 * @return 登出请求消息
	 */
	protected ILogoutReq createLogoutReq() {
		LogoutReq logoutReq = new LogoutReq();
		return logoutReq;
	}
}
