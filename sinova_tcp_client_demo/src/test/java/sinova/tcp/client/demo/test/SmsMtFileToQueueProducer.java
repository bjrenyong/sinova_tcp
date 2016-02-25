package sinova.tcp.client.demo.test;

import java.util.Observable;
import java.util.Observer;

import sinova.tcp.framework.common.TcpAppStatus;
import sinova.tcp.framework.test.AbsFileToQueueProducer;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.standard.demo.SmsMtReq;
import sinovatech.components.msgqueue.IQueueProduceConsumeClient;

public class SmsMtFileToQueueProducer extends AbsFileToQueueProducer<SmsMtReq> implements Observer {

	/** TCP客户端状态，依赖注入 */
	private TcpAppStatus tcpClientStatus;
	/** 短信下行队列生产者，依赖注入 */
	private IQueueProduceConsumeClient<IReq> smsMtQueue;

	public void init() {
		// 监听TCP client状态
		tcpClientStatus.addObserver(this);
		new Thread(this).start();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (tcpClientStatus.getStatus() == TcpAppStatus.Status.STAUTS_CLOSING
				|| tcpClientStatus.getStatus() == TcpAppStatus.Status.STAUTS_CLOSED) {
			this.setRunFlag(false);
			this.notifyService();
		}
	}

	@Override
	protected SmsMtReq readMsg(String lineStr) throws Exception {
		String[] smsMtReqArr = lineStr.split("\\t");
		if (smsMtReqArr.length < 4) {
			throw new Exception("line error! lineStr=" + lineStr);
		}
		long clientMtSequence = Long.parseLong(smsMtReqArr[0]);
		String mobileNum = smsMtReqArr[1];
		String smsContent = smsMtReqArr[2];
		int priority = Integer.parseInt(smsMtReqArr[3]);
		SmsMtReq smsMtReq = new SmsMtReq();
		smsMtReq.setClientMtSequence(clientMtSequence);
		smsMtReq.setMobileNum(mobileNum);
		smsMtReq.setSmsContent(smsContent);
		smsMtReq.setPriority(priority);
		return smsMtReq;
	}

	@Override
	protected void sendMsgToQueue(SmsMtReq msg) {
		smsMtQueue.send(msg);
	}

	public void setTcpClientStatus(TcpAppStatus tcpClientStatus) {
		this.tcpClientStatus = tcpClientStatus;
	}

	public void setSmsMtQueue(IQueueProduceConsumeClient<IReq> smsMtQueue) {
		this.smsMtQueue = smsMtQueue;
	}

}
