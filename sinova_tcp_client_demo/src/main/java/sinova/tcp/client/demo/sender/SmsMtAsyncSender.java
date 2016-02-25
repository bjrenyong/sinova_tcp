package sinova.tcp.client.demo.sender;

import io.netty.channel.Channel;

import java.util.Observable;
import java.util.Observer;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.client.service.ClientConnectService;
import sinova.tcp.framework.common.TcpAppStatus;
import sinova.tcp.framework.common.exception.BusinessException;
import sinova.tcp.framework.common.sender.AsyncReqSendThread;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.standard.demo.ResendInfo;
import sinova.tcp.protocol.standard.demo.SmsMtReq;
import sinovatech.components.msgqueue.IQueueProduceConsumeClient;

@Service
public class SmsMtAsyncSender extends AsyncReqSendThread implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(SmsMtAsyncSender.class);
	/** 失败重发次数 */
	private static final int RESEND_MAX = 10;
	/** 发送失败可重发的错误码数组 */
	private static final String[] RESENDABLE_CODES = { "-1", "-2", "-3", "-4" };

	/** TCP客户端状态，依赖注入 */
	@Autowired
	private TcpAppStatus tcpClientStatus;
	@Autowired
	private IQueueProduceConsumeClient<IReq> smsMtQueue;
	@Autowired
	private ClientConnectService clientConnectService;

	@PostConstruct
	public void init() {
		// 监听TCP client状态
		tcpClientStatus.addObserver(this);
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
		// 如果TCP客户端状态为关闭中或已关闭，而TCP连接状态为INIT、ALLOW_CONNECT或DISALLOW_CONNECT，则将发送线程的运行状态设置为false
		if (tcpClientStatus.getStatus() == TcpAppStatus.Status.STAUTS_CLOSING
				|| tcpClientStatus.getStatus() == TcpAppStatus.Status.STAUTS_CLOSED) {
			this.setRunFlag(false);
			this.notifyService();
		}
	}

	@Override
	public String getThreadName() {
		return "smsMtAsyncSender";
	}

	@Override
	public boolean isValid2Send() {
		return (clientConnectService.checkConnectStatus() == ClientConnectService.ConnectStatus.CONNECT);
	}

	@Override
	public IReq getReqMsgFromQueue() {
		return smsMtQueue.receive();
	}

	@Override
	public long getSendSleepMillis() {
		// 休眠时间设为2秒
		return 2000;
	}

	@Override
	public boolean isValidMsg(IReq reqMsg) {
		if (reqMsg instanceof SmsMtReq) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void sendReqMsg(IReq reqMsg) {
		// 对需要延时发送的做延时等待
		checkSleep(reqMsg);
		// 获取对应的channel
		Channel channel = clientConnectService.getChannel();
		if (isValidSendReqChannel(channel)) {
			sendReqByChannel(reqMsg, channel);
		} else {
			// 执行连接无效时的发送请求处理
			logger.warn("no valid channel for the message, send failed! reqMsg=" + reqMsg);
			// 由于链路问题导致发送失败的可以重发
			processSendFailedMsg(reqMsg, true);
		}
	}

	/**
	 * 对需要延时发送的消息做延时等待
	 * @param reqMsg 请求消息
	 */
	private void checkSleep(IReq reqMsg) {
		long current = System.currentTimeMillis();
		SmsMtReq smsMtReq = (SmsMtReq) reqMsg;
		ResendInfo resendInfo = smsMtReq.getResendInfo();
		if (resendInfo == null || resendInfo.getSendTimes() == 0) {
			// 是第一次发送，不进行休眠
			return;
		} else if (resendInfo.getLastSendTime() + 2000 > current) {
			// 是消息重发，而且间隔时间不够，完成剩余休眠
			this.waitMillis(resendInfo.getLastSendTime() + 2000 - current);
		} else {
			// 是消息重发，但间隔时间足够了，不进行休眠
			return;
		}
	}

	/**
	 * 是否是有效的发送通道
	 * @param channel 发送通道
	 * @return boolean
	 */
	private boolean isValidSendReqChannel(Channel channel) {
		if (channel == null) {
			return false;
		} else if (!channel.isActive()) {
			return false;
		} else {
			return true;
		}
	}

	private boolean isResendable(BusinessException be) {
		return ArrayUtils.contains(RESENDABLE_CODES, be.getErrorCode());
	}	
	/**
	 * 处理发送失败的消息
	 * @param reqMsg 请求消息
	 * @param resendable 是否为可重发的错误
	 */
	private void processSendFailedMsg(IReq reqMsg, boolean resendable) {
		if (!resendable) {
			// 不可重发的消息
			logger.warn("the message be sent failed and can't be resend, discard it! reqMsg={}", reqMsg);
			return;
		}

		// 可重发消息的处理
		SmsMtReq smsMtReq = (SmsMtReq) reqMsg;
		ResendInfo resendInfo = smsMtReq.getResendInfo();
		if (resendInfo == null) {
			resendInfo = new ResendInfo();
			resendInfo.setSendTimes(1);
			resendInfo.setLastSendTime(System.currentTimeMillis());
			smsMtReq.setResendInfo(resendInfo);
			logger.warn("the message has be sent {} times but failed, push back to sendQueue! reqMsg={}",
					resendInfo.getSendTimes(), reqMsg);
			smsMtQueue.send(smsMtReq);
		} else {
			resendInfo.setSendTimes(resendInfo.getSendTimes() + 1);
			resendInfo.setLastSendTime(System.currentTimeMillis());
			smsMtReq.setResendInfo(resendInfo);
			if (resendInfo.getSendTimes() >= RESEND_MAX) {
				logger.warn("the message has be sent {} times but failed, discard it! reqMsg={}",
						resendInfo.getSendTimes(), reqMsg);
			} else {
				logger.warn("the message has be sent {} times but failed, push back to sendQueue! reqMsg={}",
						resendInfo.getSendTimes(), reqMsg);
				smsMtQueue.send(smsMtReq);
			}
		}
	}

	private void sendReqByChannel(IReq reqMsg, Channel channel) {
		try {
			clientConnectService.businessReqSendAsync(reqMsg);
		} catch (BusinessException e) {
			logger.error(e.getMessage(), e);
			if (isResendable(e)) {
				// 可重发异常
				processSendFailedMsg(reqMsg, true);
			} else {
				// 不可重发异常
				processSendFailedMsg(reqMsg, false);
			}
		} catch (Exception e) {
			// 其它异常
			logger.error(e.getMessage(), e);
			processSendFailedMsg(reqMsg, false);
		}
	}

	@Override
	public void doWhenNotValidReqMsg(IReq reqMsg) {
		logger.warn("the request message is not valid, discard it! reqMsg=" + reqMsg);
	}

}
