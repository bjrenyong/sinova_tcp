package sinova.tcp.framework.server.service;

import java.util.Map;

import sinova.tcp.framework.server.entity.ServerUserBase;
import sinova.tcp.protocol.ILoginReq;

/**
 * 服务端用户服务接口
 * @author Timothy
 */
public interface IServerUserService {

	/**
	 * 获取用户ID-用户信息MAP
	 * @return 用户ID-用户信息MAP
	 */
	public Map<Integer, ServerUserBase> getId2UserInfoMap();

	/**
	 * 根据用户ID获取对应的服务端用户信息
	 * @param userId 用户ID
	 * @return 服务端用户信息
	 */
	public ServerUserBase getServerUserByUserId(Integer userId);

	/**
	 * 用户登录鉴权
	 * @param userBase 服务端用户信息
	 * @param req 登录请求消息
	 * @return 登录鉴权是否成功
	 */
	public boolean validateAuth(ServerUserBase userBase, ILoginReq req);
}
