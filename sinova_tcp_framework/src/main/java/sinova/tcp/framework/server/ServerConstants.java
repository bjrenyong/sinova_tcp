package sinova.tcp.framework.server;

public class ServerConstants {

	// 登录结果码常量定义
	/** 登录成功 */
	public static final String LOGIN_CODE_SUCCESS = "0";
	/** IP地址验证失败 */
	public static final String LOGIN_CODE_IP_FAIL = "1";
	/** 用户身份验证失败 */
	public static final String LOGIN_CODE_AUTH_FAIL = "2";
	/** 用户重复登录 */
	public static final String LOGIN_CODE_DUPLICATE = "3";
	/** 用户登录验证时服务端发生异常 */
	public static final String LOGIN_CODE_SERVEREXCEPTION = "4";

	// 用户连接类型常量定义
	/** 用户连接类型-双工 */
	public static final int USER_CONNECTIONTYPE_ALL = 1;
	/** 用户连接类型-仅下行 */
	public static final int USER_CONNECTIONTYPE_MT = 2;
	/** 用户连接类型-仅上行 */
	public static final int USER_CONNECTIONTYPE_MO = 3;

}
