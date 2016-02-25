package sinova.tcp.framework.common.monitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP速度计数
 * @author Timothy
 */
public class TcpSpeedRecord {

	private static final Logger logger = LoggerFactory.getLogger(TcpSpeedRecord.class);

	private static final String NAME_USERID = "userId";
	private static final String NAME_CONNECT = "connect";
	private static final String NAME_VALIDSENDTOTAL = "validSendTotal";
	private static final String NAME_SENDTOTAL = "sendTotal";
	private static final String NAME_SENDSUCCESS = "sendSuccess";
	private static final String NAME_SENDTIMEOUT = "sendTimeOut";
	private static final String NAME_SENDERROR = "sendError";
	private static final String NAME_RECEIVETOTAL = "receiveTotal";
	private static final String LoggerFormat = "userId %d connect: %b, validSendTotal: %d, sendTotal: %d(success: %d, timeout: %d, error: %d), receiveTotal: %d";

	// 用户ID
	private int userId;
	// 连接状态
	private AtomicBoolean aiConnect = new AtomicBoolean();
	// 有效发送总数
	private AtomicInteger aiValidSendTotal = new AtomicInteger(0);
	// 发送总数
	private AtomicInteger aiSendTotoal = new AtomicInteger(0);
	// 发送成功数
	private AtomicInteger aiSendSuccess = new AtomicInteger(0);
	// 发送超时数
	private AtomicInteger aiSendTimeout = new AtomicInteger(0);
	// 发送失败数
	private AtomicInteger aiSendError = new AtomicInteger(0);
	// 接收总数
	private AtomicInteger aiReceiveTotal = new AtomicInteger(0);

	private boolean lastConnect = false;
	private int lastValidSendTotal = 0;
	private int lastSendTotal = 0;
	private int lastSendSuccess = 0;
	private int lastSendTimeout = 0;
	private int lastSendError = 0;
	private int lastReceiveTotal = 0;

	public TcpSpeedRecord(int userId) {
		this.userId = userId;
	}

	public void setConnectStatus(boolean connectStatus) {
		this.aiConnect.set(connectStatus);
	}

	public void addValidSendTotal(int count) {
		aiValidSendTotal.addAndGet(count);
	}

	public void addSendSuccessCount(int count) {
		aiSendSuccess.addAndGet(count);
		aiSendTotoal.addAndGet(count);
	}

	public void addSendTimeoutCount(int count) {
		aiSendTimeout.addAndGet(count);
		aiSendTotoal.addAndGet(count);
	}

	public void addSendErrorCount(int count) {
		aiSendError.addAndGet(count);
		aiSendTotoal.addAndGet(count);
	}

	public void addReceiveTotal(int count) {
		aiReceiveTotal.addAndGet(count);
	}

	public void setAndClearRecord() {
		this.lastConnect = aiConnect.get();
		this.lastValidSendTotal = aiValidSendTotal.getAndSet(0);
		this.lastSendTotal = aiSendTotoal.getAndSet(0);
		this.lastSendSuccess = aiSendSuccess.getAndSet(0);
		this.lastSendTimeout = aiSendTimeout.getAndSet(0);
		this.lastSendError = aiSendError.getAndSet(0);
		this.lastReceiveTotal = aiReceiveTotal.getAndSet(0);
		logger.info(String.format(LoggerFormat, userId, lastConnect, lastValidSendTotal, lastSendTotal,
				lastSendSuccess, lastSendTimeout, lastSendError, lastReceiveTotal));
	}

	public Map<String, Object> getRecord() {
		Map<String, Object> recordMap = new LinkedHashMap<String, Object>();
		recordMap.put(NAME_USERID, this.userId);
		recordMap.put(NAME_CONNECT, this.lastConnect);
		recordMap.put(NAME_VALIDSENDTOTAL, this.lastValidSendTotal);
		recordMap.put(NAME_SENDTOTAL, this.lastSendTotal);
		recordMap.put(NAME_SENDSUCCESS, this.lastSendSuccess);
		recordMap.put(NAME_SENDTIMEOUT, this.lastSendTimeout);
		recordMap.put(NAME_SENDERROR, this.lastSendError);
		recordMap.put(NAME_RECEIVETOTAL, this.lastReceiveTotal);
		return recordMap;
	}

}
