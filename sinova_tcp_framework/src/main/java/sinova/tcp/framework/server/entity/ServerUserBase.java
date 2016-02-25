package sinova.tcp.framework.server.entity;

import sinova.tcp.framework.common.monitor.TcpSpeedRecord;
import sinova.tcp.framework.util.SlidingWindow;

/**
 * 服务端用户信息基础类<br/>
 */
public class ServerUserBase {
	/** 用户ID */
	private int userId;
	/** 上行滑动窗口大小 */
	private int moWindowSize;
	/** 滑动窗口 */
	private SlidingWindow slidingWindow;
	/** 连接类型: 1-全双工;2-仅下行;3-仅上行 */
	private int connectionType;
	/** 服务端速度计数 */
	private TcpSpeedRecord serverSpeedRecord;
	/** 服务端发送业务请求限速值，小于等于0表示不限速 */
	private int sendSpeedMax;
	/** 服务端接收业务请求限速值，小于等于0表示不限速 */
	private int receiveSpeedMax;

	public ServerUserBase(int userId, int moWindowSize, int connectionType, int sendSpeedMax, int receiveSpeedMax) {
		this.userId = userId;
		this.moWindowSize = moWindowSize;
		this.connectionType = connectionType;
		this.slidingWindow = new SlidingWindow(moWindowSize, 30);
		this.serverSpeedRecord = new TcpSpeedRecord(userId);
		this.sendSpeedMax = sendSpeedMax;
		this.receiveSpeedMax = receiveSpeedMax;
	}

	public int getUserId() {
		return userId;
	}

	public int getMoWindowSize() {
		return moWindowSize;
	}

	public SlidingWindow getSlidingWindow() {
		return slidingWindow;
	}

	public int getConnectionType() {
		return connectionType;
	}

	public TcpSpeedRecord getServerSpeedRecord() {
		return serverSpeedRecord;
	}

	public int getSendSpeedMax() {
		return sendSpeedMax;
	}

	public int getReceiveSpeedMax() {
		return receiveSpeedMax;
	}

}
