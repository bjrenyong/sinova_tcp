package sinova.tcp.framework.client.service;

import io.netty.channel.Channel;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.client.NettyClient;
import sinova.tcp.framework.common.FrameworkConstants;
import sinova.tcp.framework.common.TcpAppStatus;
import sinova.tcp.framework.common.TcpErrorCode;
import sinova.tcp.framework.common.exception.BusinessException;
import sinova.tcp.framework.common.monitor.IOverWaitSpeed;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.common.service.TransportMsgService;
import sinova.tcp.framework.common.window.SyncWindowMsg;
import sinova.tcp.framework.common.window.WindowMsg;
import sinova.tcp.framework.util.SlidingWindow;
import sinova.tcp.protocol.IMsg;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;
import sinova.tcp.protocol.simple.ErrorResp;

/**
 * 客户端连接服务<br/>
 * 通过监控TCP客户端状态来调整服务自身的状态<br/>
 * @author Timothy
 */
@Service
public class ClientConnectService extends Thread implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(ClientConnectService.class);
	private static final int THREAD_WAIT_SECOND = 30;

	/** 连接服务状态枚举类型 */
	public enum ConnectServiceStatus {
		// 初始
		INIT,
		// 允许连接
		ALLOW_CONNECT,
		// 不允许连接
		DISALLOW_CONNECT,
		// 关闭中
		CLOSEING,
		// 已关闭
		CLOSED
	}

	/** netty连接状态 */
	public enum ConnectStatus {
		// 断开
		DISCONNECT,
		// 连接
		CONNECT
	};

	/** 传输消息服务，依赖注入 */
	@Autowired
	private TransportMsgService transportMsgService;
	/** TCP客户端状态，依赖注入 */
	@Autowired
	private TcpAppStatus tcpClientStatus;
	/** netty客户端 */
	@Resource
	private NettyClient nettyClient;
	/** 发送滑动窗口大小，依赖注入，默认为32 */
	@Value("${netty.client.window.size:32}")
	private int windowSize;
	/** 发送滑动窗口内消息超时时间，依赖注入，默认为30秒 */
	@Value("${netty.client.window.timeoutsecond:30}")
	private int windowMsgTimeOutSecond;
	/** 客户端发送限速开关，默认开启限速 */
	@Value("${netty.client.send.speed_limit_flag:true}")
	private boolean clientSendSpeedLimitFlag;
	/** 客户端侧用户服务 */
	@Autowired
	private IClientUserService clientUserService;
	/** 客户端发送限速器 */
	@Resource
	private IOverWaitSpeed clientSendSecondSpeed;

	/** 客户端连接服务线程同步锁 */
	private byte[] lock = new byte[1];
	/** 连接服务状态 */
	private ConnectServiceStatus connectServiceStatus = ConnectServiceStatus.INIT;
	/** netty连接通道 */
	private Channel channel;
	/** 连接类型：1-全双工;2-仅下行;3-仅上行 */
	private int connectionType;
	/** netty连接通道对应的滑动窗口 */
	private SlidingWindow slidingWindow;

	/**
	 * 客户端连接服务初始化方法<br/>
	 * 1. 设置为TCP客户端状态的监听者<br/>
	 * 2. 启动客户端连接服务线程
	 */
	@PostConstruct
	public void init() {
		// 初始化滑动窗口
		this.slidingWindow = new SlidingWindow(windowSize, windowMsgTimeOutSecond);
		// 监听TCP client状态
		tcpClientStatus.addObserver(this);
		// 启动自身线程来监控连接服务
		this.start();
	}

	/**
	 * TCP客户端状态变化触发客户端连接状态的调整<br/>
	 * 如果TCP客户端状态为active，而TCP连接状态为INIT，则将TCP连接状态置为ALLOW_CONNECT<br/>
	 * 如果TCP客户端状态为关闭中或已关闭，而TCP连接状态为INIT、ALLOW_CONNECT或DISALLOW_CONNECT，则将TCP连接状态置为关闭中
	 * @param o Observable(实为TCP客户端状态)
	 * @param arg 相关参数
	 */
	@Override
	public void update(Observable o, Object arg) {
		// 观察TCP客户端状态
		// 如果TCP客户端状态为active，而TCP连接状态为INIT，则将TCP连接状态置为ALLOW_CONNECT
		// 如果TCP客户端状态为关闭中或已关闭，而TCP连接状态为INIT、ALLOW_CONNECT或DISALLOW_CONNECT，则将TCP连接状态置为关闭中
		if (tcpClientStatus.getStatus() == TcpAppStatus.Status.STATUS_ACTIVE) {
			if (this.connectServiceStatus == ConnectServiceStatus.INIT) {
				this.connectServiceStatus = ConnectServiceStatus.ALLOW_CONNECT;
				notifyService();
			}
		} else if (tcpClientStatus.getStatus() == TcpAppStatus.Status.STAUTS_CLOSING
				|| tcpClientStatus.getStatus() == TcpAppStatus.Status.STAUTS_CLOSED) {
			if (isAllowRunStatus()) {
				this.connectServiceStatus = ConnectServiceStatus.CLOSEING;
				notifyService();
			}
		}
	}

	public Channel getChannel() {
		return channel;
	}

	/**
	 * 设定netty channel<br/>
	 * 当应用建立起真正有效的netty channel后(连接并登录成功)，需要调用此方法来缓存
	 * @param channel Channel对象
	 */
	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	/**
	 * 设置连接类型
	 * @param connectionType 连接类型
	 */
	public void setConnectionType(int connectionType) {
		this.connectionType = connectionType;
	}

	/**
	 * 检查客户端的连接状态
	 * @return ConnectStatus
	 */
	public ConnectStatus checkConnectStatus() {
		if (channel == null) {
			return ConnectStatus.DISCONNECT;
		} else if (channel.isActive()) {
			return ConnectStatus.CONNECT;
		} else {
			return ConnectStatus.DISCONNECT;
		}
	}

	/**
	 * 判断客户端是否处在连接状态
	 * @return boolean
	 */
	public boolean isTcpConnect() {
		return checkConnectStatus() == ConnectStatus.CONNECT;
	}

	/**
	 * 判断客户端连接服务线程是否允许继续运行
	 * @return true:允许继续运行; false:不允许继续运行
	 */
	public boolean isAllowRunStatus() {
		if (connectServiceStatus != ConnectServiceStatus.CLOSEING
				&& connectServiceStatus != ConnectServiceStatus.CLOSED) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 发起netty连接<br/>
	 * 没有有效连接的需建立新连接<br/>
	 * 已经有有效连接的则什么都不做
	 */
	private void doConnect() {
		// 如果netty连接已经存在，但连接已失效，先把失效的连接关闭回收
		if (channel != null && !channel.isActive()) {
			logger.warn("channel is not active, close it!");
			closeChannel();
		}
		// 如果netty连接不存在，发起连接请求
		if (channel == null) {
			// 发起登录请求
			nettyClient.connect();
		}
	}

	/**
	 * 客户端连接服务线程挂起休眠
	 * @param waitSeconds 挂起休眠时间，单位秒
	 */
	private void waitSeconds(int waitSeconds) {
		synchronized (lock) {
			try {
				lock.wait(waitSeconds * 1000);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * 客户端连接服务线程唤醒
	 */
	private void notifyService() {
		synchronized (lock) {
			lock.notify();
		}
	}

	/**
	 * 清理滑动窗口，用于连接关闭时
	 */
	private void clearWindow() {
		List<WindowMsg> windowMsgList = this.slidingWindow.removeAllMsg();
		// 发送超时计数
		this.clientUserService.getClientSpeedRecord().addSendTimeoutCount(windowMsgList.size());
		for (WindowMsg windowMsg : windowMsgList) {
			logger.warn("window clear! The request message removed from the sliding window: transportReqMsg="
					+ windowMsg.getTransportReqMsg());
			if (windowMsg instanceof SyncWindowMsg) {
				// 同步请求类窗口回写传输响应消息(写null)
				((SyncWindowMsg) windowMsg).setTransportRespMsg(null);
			}

		}
	}

	/**
	 * 关闭存在的连接
	 */
	private void closeChannel() {
		try {
			if (channel != null) {
				if (channel.isActive()) {
					// 发起登出请求
					this.nettyClient.disconnect(channel);
				}
				// 关闭channel
				channel.close();
				// 清理滑动窗口
				clearWindow();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		channel = null;
	}

	/**
	 * 检查并清理滑动窗口中的过期消息
	 */
	private void windowMsgTimeoutCheck() {
		logger.info("window message time out check begin");
		int clearCount = 0;
		Map<Integer, WindowMsg> windowMap = this.slidingWindow.getWindow().getMap();
		long timeOut = this.slidingWindow.getMsgTimeOutSecond() * 1000;
		long currentTime = System.currentTimeMillis();
		Iterator<Integer> keyIter = windowMap.keySet().iterator();
		while (keyIter.hasNext()) {
			int sequenceId = keyIter.next();
			WindowMsg windowMsg = windowMap.get(sequenceId);
			if (windowMsg != null && currentTime - windowMsg.getStart() > timeOut) {
				windowMsg = this.slidingWindow.removeMsg(sequenceId);
				// 在判断的这段时间仍有可能有正常的窗口匹配，因此还是要判断一下是否为空
				if (windowMsg != null) {
					logger.warn("the window message time out, discard it! message=" + windowMsg.getTransportReqMsg());
					clearCount++;
					if (windowMsg instanceof SyncWindowMsg) {
						// 同步请求类窗口回写传输响应消息(写null)
						((SyncWindowMsg) windowMsg).setTransportRespMsg(null);
					}
				}
			}
		}
		// 发送超时计数
		this.clientUserService.getClientSpeedRecord().addSendTimeoutCount(clearCount);
		logger.info("window message time out check end! clearCount=" + clearCount + ", recordCount="
				+ this.slidingWindow.getWindowRecordCount());
	}

	/**
	 * 客户端连接服务线程运行
	 */
	public void run() {
		while (isAllowRunStatus()) {
			// 处在允许连接服务运行的状态
			try {
				// 检查并清理滑动窗口中的过期消息
				windowMsgTimeoutCheck();
				if (this.connectServiceStatus == ConnectServiceStatus.ALLOW_CONNECT) {
					// 连接服务状态为允许连接，执行连接检查，确保建立连接
					doConnect();
				} else if (this.connectServiceStatus == ConnectServiceStatus.DISALLOW_CONNECT) {
					// 连接服务状态为不允许连接，执行连接检查，确保断开连接
					closeChannel();
				}
				waitSeconds(THREAD_WAIT_SECOND);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				waitSeconds(THREAD_WAIT_SECOND);
			}
		}
		// 断开连接
		closeChannel();
		// 设置连接服务状态为已关闭
		this.connectServiceStatus = ConnectServiceStatus.CLOSED;
	}

	/**
	 * 客户端连接服务由未连接转变为已连接
	 */
	public void disconnect2Connect() {
		if (this.connectServiceStatus == ConnectServiceStatus.DISALLOW_CONNECT) {
			this.connectServiceStatus = ConnectServiceStatus.ALLOW_CONNECT;
			notifyService();
		}
	}

	/**
	 * 客户端连接服务由已连接转变为未连接
	 */
	public void connect2disconnect() {
		if (this.connectServiceStatus == ConnectServiceStatus.ALLOW_CONNECT) {
			this.connectServiceStatus = ConnectServiceStatus.DISALLOW_CONNECT;
			notifyService();
		}
	}

	/**
	 * 判断客户端是否被允许接收业务请求
	 * @return boolean
	 */
	public boolean isAllowReqReceive() {
		if (this.connectionType == FrameworkConstants.CONNECTION_TYPE_ALL
				|| this.connectionType == FrameworkConstants.CONNECTION_TYPE_MO) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断客户端是否被允许业务请求发送
	 * @return boolean
	 */
	private boolean isAllowReqSend() {
		if (this.connectionType == FrameworkConstants.CONNECTION_TYPE_ALL
				|| this.connectionType == FrameworkConstants.CONNECTION_TYPE_MT) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 将请求传输消息封装成窗口消息
	 * @param transportReqMsg 请求传输消息
	 * @param sync 是否为同步调用
	 * @return 封装的窗口消息
	 */
	private WindowMsg package2WindowMsg(TransportMsg transportReqMsg, boolean sync) {
		if (!sync) {
			return new WindowMsg(transportReqMsg);
		} else {
			return new SyncWindowMsg(transportReqMsg);
		}
	}

	/**
	 * 执行业务请求发送<br/>
	 * 对于可以重发的错误，抛出可重发异常ResendableException<br/>
	 * 对于不可以重发的错误，抛出普通异常Exception<br/>
	 * @param req 业务请求消息
	 * @param sync 是否为同步调用
	 * @return 业务请求对应的窗口消息
	 * @throws BusinessException
	 */
	private WindowMsg businessReqSend(IReq req, boolean sync) throws BusinessException {
		// 判断客户端是否被允许业务请求发送
		if (!isAllowReqSend()) {
			// 连接类型决定了客户端不被允许业务请求发送
			logger.warn("the connect type not allow send business request, discard the message! userId="
					+ clientUserService.getUserId());
			// 没有发出去的也算发送失败，进行失败计数
			this.clientUserService.getClientSpeedRecord().addSendErrorCount(1);
			// 连接类型不允许发送消息到服务端，不可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_NOTALLOW_MT);
		}
		// 请求消息包装成请求传输消息
		TransportMsg transportMsg = transportMsgService.req2TransportMsg(req);
		if (transportMsg == null) {
			// 请求消息包装成请求传输消息失败
			logger.warn("req to transportMsg failed, can't send and discard! req=" + req);
			// 没有发出去的也算发送失败，进行失败计数
			this.clientUserService.getClientSpeedRecord().addSendErrorCount(1);
			// 消息格式错误，不可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_MESSAGE_FORMAT);
		}

		try {
			// 请求传输消息包装成窗口消息
			WindowMsg windowMsg = package2WindowMsg(transportMsg, sync);
			// 判断是否需要进行发送限速？（判断：发送限速开启 && 设置了发送限速器）
			if (clientSendSpeedLimitFlag && this.clientSendSecondSpeed != null) {
				// 执行发送限速计数
				this.clientSendSecondSpeed.limitSpeed();
			}
			logger.info("send a message to server! mtReq={}", req);
			slidingWindow.putMsg(transportMsg.getSequenceId(), windowMsg);
			this.channel.writeAndFlush(transportMsg);
			// 有效发送数+1
			this.clientUserService.getClientSpeedRecord().addValidSendTotal(1);
			return windowMsg;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			slidingWindow.removeMsg(transportMsg.getSequenceId());
			// 发送失败数+1
			this.clientUserService.getClientSpeedRecord().addSendErrorCount(1);
			// 系统异常（可重发）
			throw new BusinessException(TcpErrorCode.ERRORCODE_SYSTEM_EXCEPTION);
		}
	}

	/**
	 * 异步业务请求消息发送<br/>
	 * 对于可以重发的错误，抛出可重发异常ResendableException<br/>
	 * 对于不可以重发的错误，抛出普通异常Exception<br/>
	 * @param req 业务请求消息
	 * @throws BusinessException
	 */
	public void businessReqSendAsync(IReq req) throws BusinessException {
		businessReqSend(req, false);
	}

	/**
	 * 同步业务请求消息发送
	 * @param req 业务请求消息
	 * @return 请求对应的响应消息
	 * @throws BusinessException
	 */
	public IResp businessReqSendSync(IReq req) throws BusinessException {
		// 同步业务请求消息发送
		SyncWindowMsg syncWindowMsg = (SyncWindowMsg) this.businessReqSend(req, true);
		TransportMsg transportRespMsg = syncWindowMsg.get();
		if (transportRespMsg == null) {
			// 发送超时，可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_TIMEOUT);
		}
		IMsg respMsg = transportRespMsg.getMsg();
		if (respMsg == null) {
			// 发送超时，可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_TIMEOUT);
		} else if (respMsg instanceof ErrorResp) {
			// 发送处理失败，是否可重发看具体的错误码
			throw new BusinessException((ErrorResp) respMsg);
		} else if (respMsg instanceof IResp) {
			logger.info("sync send a message success! respMsg=" + respMsg);
			return (IResp) respMsg;
		} else {
			logger.error("response type error! respMsg=" + respMsg);
			// 消息格式错误，不可重发
			throw new BusinessException(TcpErrorCode.ERRORCODE_MESSAGE_FORMAT);
		}
	}

	/**
	 * 传输响应消息匹配滑动窗口消息
	 * @param transportRespMsg 传输响应消息
	 * @return 滑动窗口消息
	 */
	public WindowMsg responseMatchWindowMsg(TransportMsg transportRespMsg) {
		int sequenceId = transportRespMsg.getSequenceId();
		WindowMsg windowMsg = slidingWindow.removeMsg(sequenceId);
		if (windowMsg == null) {
			logger.warn("sliding windows match response failed! response=" + transportRespMsg.getMsg());
			return null;
		} else {
			// 收到传输响应消息且找到匹配的滑动窗口，意味着业务请求发送成功，发送成功计数
			this.clientUserService.getClientSpeedRecord().addSendSuccessCount(1);
			if (windowMsg instanceof SyncWindowMsg) {
				// 是同步调用
				((SyncWindowMsg) windowMsg).setTransportRespMsg(transportRespMsg);
			}
			return windowMsg;
		}
	}

	public void setConnectServiceStatus(ConnectServiceStatus connectServiceStatus) {
		this.connectServiceStatus = connectServiceStatus;
	}

	public NettyClient getNettyClient() {
		return nettyClient;
	}

}
