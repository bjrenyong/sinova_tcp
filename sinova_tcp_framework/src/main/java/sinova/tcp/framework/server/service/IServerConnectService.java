package sinova.tcp.framework.server.service;

import io.netty.channel.Channel;

import java.util.Map;

import sinova.tcp.framework.common.exception.BusinessException;
import sinova.tcp.framework.common.protocol.TransportMsg;
import sinova.tcp.framework.common.window.WindowMsg;
import sinova.tcp.protocol.ILoginReq;
import sinova.tcp.protocol.ILoginResp;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 服务端的Netty连接管理
 * @author Timothy
 * @param <T> 用户信息类
 */
public interface IServerConnectService<T> {

	/**
	 * 响应登录请求并返回登录结果
	 * @param loginReq 登录请求
	 * @return 登录结果
	 */
	public ILoginResp login(ILoginReq loginReq, Channel channel);

	/**
	 * 响应登出请求
	 * @param channel 连接通道
	 */
	public void logout(Channel channel);

	/**
	 * 获取某个连接通道所归属的用户
	 * @param channel netty Channel
	 * @return 通道所归属的用户
	 */
	public T getUserByChannel(Channel channel);

	/**
	 * 获取某个用户的连接通道
	 * @param userId 用户ID
	 * @return 该用户的连接通道
	 */
	public Channel getChannelByUserId(Integer userId);

	/**
	 * 获取用户的输出型连接通道MAP(即上行连接通道，服务端发送数据用连接通道)
	 * @return 用户的输出型连接通道MAP
	 */
	public Map<Integer, Channel> getOutBoundConnectMap();

	/**
	 * 获取连接通道-用户关系MAP
	 * @return 连接通道-用户关系MAP
	 */
	public Map<Channel, T> getChannel2UserMap();

	/**
	 * 业务请求消息异步发送
	 * @param req 业务请求消息
	 * @param channel 发送所使用的连接通道
	 * @throws BusinessException
	 */
	public void businessReqSendAsync(IReq req, Channel channel) throws BusinessException;

	/**
	 * 业务请求消息同步发送
	 * @param req 业务请求消息
	 * @param channel 发送所使用的连接通道
	 * @return 业务响应消息
	 * @throws BusinessException
	 */
	public IResp businessReqSendSync(IReq req, Channel channel) throws BusinessException;

	/**
	 * 获取匹配传输响应消息的窗口消息（含传输请求消息）
	 * @param transportRespMsg 传输响应消息
	 * @param channel 指定的连接通道
	 * @return 匹配传输响应消息的窗口消息(匹配不到返回null)
	 */
	public WindowMsg responseMatchWindowMsg(TransportMsg transportRespMsg, Channel channel);
}
