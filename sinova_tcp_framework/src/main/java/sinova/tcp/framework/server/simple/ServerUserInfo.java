package sinova.tcp.framework.server.simple;

import sinova.tcp.framework.server.entity.ServerUserBase;

/**
 * 服务端用户信息-缺省实现
 * @author Timothy
 */
public class ServerUserInfo extends ServerUserBase {

	/** 登录名 */
	private String userName;
	/** 登录密码 */
	private String password;
	/** 用户IP地址 */
	private String ip;
	/** 密钥 */
	private String secretKey;

	public ServerUserInfo(int userId, String userName, String password, String ip, String secretKey,
			int connectionType, int moWindowSize, int sendSpeedMax, int receiveSpeedMax) {
		super(userId, moWindowSize, connectionType, sendSpeedMax, receiveSpeedMax);
		this.userName = userName;
		this.password = password;
		this.ip = ip;
		this.secretKey = secretKey;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getIp() {
		return ip;
	}

	public String getSecretKey() {
		return secretKey;
	}

}