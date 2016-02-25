package sinova.tcp.framework.common;

/**
 * Created by haoxiaodong on 2016-1-18.<br/>
 */
public class FrameworkConstants {
	public static final short COMMANDID_ERROR = 9001;
	public static final String ACTION_ERROR = "9002";

	/** 连接类型-全双工 */
	public static final int CONNECTION_TYPE_ALL = 1;
	/** 连接类型-仅下行 */
	public static final int CONNECTION_TYPE_MT = 2;
	/** 连接类型-仅上行 */
	public static final int CONNECTION_TYPE_MO = 3;

	/** 错误码-速度超限 */
	public static final String ERRORCODE_SPEEDOVER = "-1";
}
