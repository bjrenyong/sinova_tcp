package sinova.tcp.framework.server.simple;

import sinova.tcp.framework.server.entity.ServerUserBase;
import sinova.tcp.framework.server.service.IServerUserService;
import sinova.tcp.framework.util.MD5Util;
import sinova.tcp.protocol.ILoginReq;
import sinova.tcp.protocol.simple.LoginReq;

/**
 * 服务端用户信息服务抽象类-缺省实现
 */
public abstract class AbsServerUserService implements IServerUserService {

	/**
	 * 用户登录信息鉴权
	 * @param serverUserInfo 用户信息
	 * @return 登录鉴权结果
	 */
	@Override
	public boolean validateAuth(ServerUserBase serverUserInfo, ILoginReq loginReq) {
		ServerUserInfo userInfo = (ServerUserInfo) serverUserInfo;
		LoginReq req = (LoginReq) loginReq;
		if (userInfo == null || req.getAuthOrigin() == null || req.getAuthOrigin().length() <= 0) {
			return false;
		}

		return req.getAuthOrigin().equals(
				MD5Util.encrypt(userInfo.getIp() + userInfo.getSecretKey() + req.getTimestamp()));
	}
}
