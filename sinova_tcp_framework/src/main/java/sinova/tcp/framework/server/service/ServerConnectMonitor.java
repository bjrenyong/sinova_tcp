package sinova.tcp.framework.server.service;

import io.netty.channel.Channel;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.common.TcpAppStatus;
import sinova.tcp.framework.common.window.SyncWindowMsg;
import sinova.tcp.framework.common.window.WindowMsg;
import sinova.tcp.framework.server.entity.ServerUserBase;
import sinova.tcp.framework.util.SlidingWindow;

/**
 * 服务端连接监控线程<br/>
 * 服务端的连接监控线程比较简单，它不需要检查连接状态并根据需要请求或断开连接，只需要检查各上行连接的滑动窗口中是否存在超时等待的请求
 * @author Timothy
 */
@Service
public class ServerConnectMonitor implements Runnable, Observer {

	private static final Logger logger = LoggerFactory.getLogger(ServerConnectMonitor.class);
	private static final int THREAD_WAIT_SECOND = 30;

	@Autowired
	private TcpAppStatus tcpServerStatus;
	@Autowired
	private IServerConnectService<ServerUserBase> serverConnectService;

	/** 服务端连接监控线程同步锁 */
	private byte[] lock = new byte[1];

	@PostConstruct
	public void init() {
		tcpServerStatus.addObserver(this);
		new Thread(this).start();
	}

	@Override
	public void update(Observable o, Object arg) {
		// 服务端比较简单，只需要唤醒监控线程即可
		notifyMe();
	}

	@Override
	public void run() {
		logger.info("thread ServerConnectMonitor begin!");
		while (isAllowRunStatus()) {
			// 处在允许连接服务运行的状态
			// 服务端所有连接的滑动窗口的超时消息检查
			allWindowMsgTimeoutCheck();
			// 完成一次业务处理后休眠等待
			waitSeconds(THREAD_WAIT_SECOND);
		}
		// 清空服务端所有连接的滑动窗口
		allWindowMsgClear();
		logger.info("thread ServerConnectMonitor end!");
	}

	/**
	 * 唤醒监控线程
	 */
	public void notifyMe() {
		synchronized (lock) {
			lock.notify();
		}
	}

	/**
	 * 判断是否允许监控线程继续存活
	 * @return boolean
	 */
	private boolean isAllowRunStatus() {
		if (tcpServerStatus.getStatus() != TcpAppStatus.Status.STAUTS_CLOSING
				&& tcpServerStatus.getStatus() != TcpAppStatus.Status.STAUTS_CLOSED) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检查并清理滑动窗口中的过期消息
	 * @param userInfo 用户信息
	 */
	private void windowMsgTimeoutCheck(ServerUserBase userInfo) {
		logger.info("window message timeout check begin! userId=" + userInfo.getUserId());
		int clearCount = 0;
		SlidingWindow slidingWindow = userInfo.getSlidingWindow();
		Map<Integer, WindowMsg> windowMap = slidingWindow.getWindow().getMap();
		long timeOut = slidingWindow.getMsgTimeOutSecond() * 1000;
		long currentTime = System.currentTimeMillis();
		Iterator<Integer> keyIter = windowMap.keySet().iterator();
		while (keyIter.hasNext()) {
			int sequenceId = keyIter.next();
			WindowMsg windowMsg = windowMap.get(sequenceId);
			if (windowMsg != null && currentTime - windowMsg.getStart() > timeOut) {
				windowMsg = slidingWindow.removeMsg(sequenceId);
				// 在判断的这段时间仍有可能有正常的窗口匹配，因此还是要判断一下是否为空
				if (windowMsg != null) {
					logger.warn("the window message timeout, discard it! message=" + windowMsg.getTransportReqMsg());
					clearCount++;
					if (windowMsg instanceof SyncWindowMsg) {
						// 同步请求类窗口回写传输响应消息(写null)
						((SyncWindowMsg) windowMsg).setTransportRespMsg(null);
					}
				}
			}
		}
		// 发送超时计数
		userInfo.getServerSpeedRecord().addSendTimeoutCount(clearCount);
		logger.info("window message timeout check end! userId=" + userInfo.getUserId() + ", clearCount=" + clearCount
				+ ", recordCount=" + slidingWindow.getWindowRecordCount());
	}

	/**
	 * 服务端所有连接的滑动窗口的超时消息检查
	 */
	private void allWindowMsgTimeoutCheck() {
		// 严格的说，应该是取出所有可以上行的连接，不需要处理那些仅支持下行的连接，考虑影响不大，不做那么细致了
		Map<Channel, ServerUserBase> channel2UserMap = serverConnectService.getChannel2UserMap();
		Iterator<Channel> keyIter = channel2UserMap.keySet().iterator();
		while (keyIter.hasNext()) {
			Channel channel = keyIter.next();
			ServerUserBase userInfo = channel2UserMap.get(channel);
			if (userInfo != null) {
				this.windowMsgTimeoutCheck(userInfo);
			}
		}
	}

	/**
	 * 清空服务端所有连接的滑动窗口
	 */
	private void allWindowMsgClear() {
		// 严格的说，应该是取出所有可以上行的连接，不需要处理那些仅支持下行的连接，考虑影响不大，不做那么细致了
		Map<Channel, ServerUserBase> channel2UserMap = serverConnectService.getChannel2UserMap();
		Iterator<Channel> keyIter = channel2UserMap.keySet().iterator();
		while (keyIter.hasNext()) {
			Channel channel = keyIter.next();
			ServerUserBase userInfo = channel2UserMap.get(channel);
			if (userInfo != null) {
				// 清空用户的滑动窗口
				List<WindowMsg> windowMsgList = userInfo.getSlidingWindow().removeAllMsg();
				// 发送超时计数
				userInfo.getServerSpeedRecord().addSendTimeoutCount(windowMsgList.size());
				for (WindowMsg windowMsg : windowMsgList) {
					logger.warn("window clear! The request message removed from the sliding window: transportReqMsg="
							+ windowMsg.getTransportReqMsg());
					if (windowMsg instanceof SyncWindowMsg) {
						// 同步请求类窗口回写传输响应消息(写null)
						((SyncWindowMsg) windowMsg).setTransportRespMsg(null);
					}
				}
			}
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

}
