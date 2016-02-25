package sinova.tcp.server.demo.test;

import java.util.Observable;
import java.util.Observer;

import sinova.tcp.framework.common.TcpAppStatus;
import sinova.tcp.framework.test.AbsFileToQueueProducer;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.standard.demo.SmsReportReq;
import sinovatech.components.msgqueue.IQueueProduceConsumeClient;

public class SmsReportFileToQueueProducer extends AbsFileToQueueProducer<SmsReportReq> implements Observer{

	/** TCP服务端状态，依赖注入 */
	private TcpAppStatus tcpServerStatus;
	/** 状态报告上行队列生产者，依赖注入 */
	private IQueueProduceConsumeClient<IReq> smsReportQueue;

	public void init() {
		// 监听TCP client状态
		tcpServerStatus.addObserver(this);
		new Thread(this).start();
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
	protected SmsReportReq readMsg(String lineStr) throws Exception {
		String[] data = lineStr.split("\\t");
		if (data.length < 4) {
			throw new Exception("line error! lineStr=" + lineStr);
		}
		int userId = Integer.parseInt(data[0]);
		long clientMtSequence = Long.parseLong(data[1]);
		long sysMtSequence = Long.parseLong(data[2]);
		String reportCode = data[3];
		SmsReportReq req = new SmsReportReq();
		req.setUserId(userId);
		req.setClientMtSequence(clientMtSequence);
		req.setSysMtSequence(sysMtSequence);
		req.setReportCode(reportCode);
		return req;
	}

	@Override
	protected void sendMsgToQueue(SmsReportReq msg) {
		smsReportQueue.send(msg);
	}

	public void setTcpServerStatus(TcpAppStatus tcpServerStatus) {
		this.tcpServerStatus = tcpServerStatus;
	}

	public void setSmsReportQueue(IQueueProduceConsumeClient<IReq> smsReportQueue) {
		this.smsReportQueue = smsReportQueue;
	}

}
