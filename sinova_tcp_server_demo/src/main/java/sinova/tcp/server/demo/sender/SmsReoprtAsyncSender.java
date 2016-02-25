package sinova.tcp.server.demo.sender;

import io.netty.channel.Channel;

import java.util.Observable;
import java.util.Observer;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.common.TcpAppStatus;
import sinova.tcp.framework.common.exception.BusinessException;
import sinova.tcp.framework.common.sender.AsyncReqSendThread;
import sinova.tcp.framework.server.entity.ServerUserBase;
import sinova.tcp.framework.server.service.IServerConnectService;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.standard.demo.ResendInfo;
import sinova.tcp.protocol.standard.demo.SmsReportReq;
import sinovatech.components.msgqueue.IQueueProduceConsumeClient;

@Service
public class SmsReoprtAsyncSender extends AsyncReqSendThread implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(SmsReoprtAsyncSender.class);
	/** 失败重发次数 */
	private static final int RESEND_MAX = 10;
	/** 发送失败可重发的错误码数组 */
	private static final String[] RESENDABLE_CODES = { "-1", "-2", "-3", "-4" };

	/** TCP服务端状态，依赖注入 */
	@Autowired
	private TcpAppStatus tcpServerStatus;
	@Autowired
	private IQueueProduceConsumeClient<IReq> smsReportQueue;
	@Autowired
	private IServerConnectService<ServerUserBase> serverConnectService;

	@PostConstruct
	public void init() {
		// 监听TCP服务端状态
		tcpServerStatus.addObserver(this);
		this.start();
	}

	/**
	 * TCP服务端状态变化触发服务端连接状态的调整<br/>
	 * 如果TCP服务端状态为active，而TCP连接状态为INIT，则将TCP连接状态置为ALLOW_CONNECT<br/>
	 * 如果TCP服务端状态为关闭中或已关闭，而TCP连接状态为INIT、ALLOW_CONNECT或DISALLOW_CONNECT，则将TCP连接状态置为关闭中
	 * @param o Observable(实为TCP服务端状态)
	 * @param arg 相关参数
	 */
	@Override
	public void update(Observable o, Object arg) {
		// 观察TCP服务端状态
		// 如果TCP服务端状态为关闭中或已关闭，而TCP连接状态为INIT、ALLOW_CONNECT或DISALLOW_CONNECT，则将发送线程的运行状态设置为false
		if (tcpServerStatus.getStatus() == TcpAppStatus.Status.STAUTS_CLOSING
				|| tcpServerStatus.getStatus() == TcpAppStatus.Status.STAUTS_CLOSED) {
			this.setRunFlag(false);
			this.notifyService();
		}
	}

	@Override
	public String getThreadName() {
		return "smsReportAsyncSender";
	}

	@Override
	public boolean isValid2Send() {
		// 一般情况是验证有没有合适的上行连接
		// 这里为了测试各种场景，做了人为的扩大！！！
		if (serverConnectService.getChannel2UserMap().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public IReq getReqMsgFromQueue() {
		return smsReportQueue.receive();
	}

	@Override
	public long getSendSleepMillis() {
		return 2000;
	}

	@Override
	public boolean isValidMsg(IReq reqMsg) {
		if (reqMsg instanceof SmsReportReq) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void sendReqMsg(IReq reqMsg) {
		// 对需要延时发送的做延时等待
		checkSleep(reqMsg);
		SmsReportReq smsReportReq = (SmsReportReq) reqMsg;
		// 获取对应的channel
		Channel channel = serverConnectService.getOutBoundConnectMap().get(smsReportReq.getUserId());
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
		SmsReportReq smsReportReq = (SmsReportReq) reqMsg;
		ResendInfo resendInfo = smsReportReq.getResendInfo();
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
		SmsReportReq smsReportReq = (SmsReportReq) reqMsg;
		ResendInfo resendInfo = smsReportReq.getResendInfo();
		if (resendInfo == null) {
			resendInfo = new ResendInfo();
			resendInfo.setSendTimes(1);
			resendInfo.setLastSendTime(System.currentTimeMillis());
			smsReportReq.setResendInfo(resendInfo);
			logger.warn("the message has be sent {} times but failed, push back to sendQueue! reqMsg={}",
					resendInfo.getSendTimes(), reqMsg);
			smsReportQueue.send(smsReportReq);
		} else {
			resendInfo.setSendTimes(resendInfo.getSendTimes() + 1);
			resendInfo.setLastSendTime(System.currentTimeMillis());
			smsReportReq.setResendInfo(resendInfo);
			if (resendInfo.getSendTimes() >= RESEND_MAX) {
				logger.warn("the message has be sent {} times but failed, discard it! reqMsg={}",
						resendInfo.getSendTimes(), reqMsg);
			} else {
				logger.warn("the message has be sent {} times but failed, push back to sendQueue! reqMsg={}",
						resendInfo.getSendTimes(), reqMsg);
				smsReportQueue.send(smsReportReq);
			}
		}
	}

	private void sendReqByChannel(IReq reqMsg, Channel channel) {
		try {
			serverConnectService.businessReqSendAsync(reqMsg, channel);
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
