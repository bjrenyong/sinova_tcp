package sinova.tcp.client.demo.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import sinova.tcp.framework.client.service.IClientUserService;
import sinova.tcp.framework.common.monitor.TcpSpeedRecord;
import sinova.tcp.framework.util.MD5Util;
import sinova.tcp.protocol.ILoginReq;
import sinova.tcp.protocol.simple.LoginReq;

@Service
public class ClientUserService implements IClientUserService {

	private DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

	private static final ResourceBundle RB_CLIENT_USER = ResourceBundle.getBundle("client_user");
	private static String KEY_USERID = "netty.client.userId";
	private static String KEY_USERNAME = "netty.client.userName";
	private static String KEY_IP = "netty.client.ip";
	private static String KEY_SECRETKEY = "netty.client.secretKey";

	private TcpSpeedRecord clientSpeedRecord;

	@PostConstruct
	public void init() {
		clientSpeedRecord = new TcpSpeedRecord(Integer.parseInt(RB_CLIENT_USER.getString(KEY_USERID)));
	}

	@Override
	public ILoginReq createLoginReq() {
		int userId = Integer.parseInt(RB_CLIENT_USER.getString(KEY_USERID));
		String userName = RB_CLIENT_USER.getString(KEY_USERNAME);
		String ip = RB_CLIENT_USER.getString(KEY_IP);
		String secretKey = RB_CLIENT_USER.getString(KEY_SECRETKEY);
		long timestamp = Long.parseLong(DATE_FORMAT.format(new Date()));
		LoginReq loginReq = new LoginReq();
		loginReq.setUserId(userId);
		loginReq.setAppName(userName);
		loginReq.setTimestamp(timestamp);
		loginReq.setAuthOrigin(MD5Util.encrypt(ip + secretKey + timestamp));
		return loginReq;
	}

	@Override
	public int getUserId() {
		return Integer.parseInt(RB_CLIENT_USER.getString(KEY_USERID));
	}

	@Override
	public TcpSpeedRecord getClientSpeedRecord() {
		return clientSpeedRecord;
	}
	
}
