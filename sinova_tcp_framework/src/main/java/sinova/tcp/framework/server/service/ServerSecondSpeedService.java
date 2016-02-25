package sinova.tcp.framework.server.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.common.FrameworkConstants;
import sinova.tcp.framework.common.monitor.IOverFalseSpeed;
import sinova.tcp.framework.common.monitor.IOverWaitSpeed;
import sinova.tcp.framework.common.monitor.SecondOverFalseSpeed;
import sinova.tcp.framework.common.monitor.SecondOverWaitSpeed;
import sinova.tcp.framework.server.entity.ServerUserBase;

/**
 * 服务端秒单位限速服务实现类
 * @author Timothy
 */
@Service
public class ServerSecondSpeedService implements IServerSpeedService {

	/** 服务端用户信息服务 */
	@Autowired
	private IServerUserService serverUserService;
	/** 发送限速：一秒被分割的份数，可注入修改，默认为5 */
	@Value("${netty.server.sendspeed.second.dividenum:5}")
	private int sendSecondDivideNum = 5;
	/** 接收限速：一秒被分割的份数，可注入修改，默认为1 */
	@Value("${netty.server.receivespeed.second.dividenum:1}")
	private int receiveSecondDivideNum = 1;
	/** 接收限速：速度值倍率，可注入修改，默认为1.2 */
	@Value("${netty.server.receivespeed.factor:1.2}")
	private float receiveSpeedCountFactor;

	/** 服务端用户ID-业务请求发送限速器MAP */
	private Map<Integer, SecondOverWaitSpeed> userId2SendSpeedMap = new HashMap<Integer, SecondOverWaitSpeed>();
	/** 服务端用户ID-业务请求接收限速器MAP */
	private Map<Integer, SecondOverFalseSpeed> userId2ReceiveSpeedMap = new HashMap<Integer, SecondOverFalseSpeed>();

	/**
	 * 服务的初始化方法
	 */
	@PostConstruct
	public void init() {
		Map<Integer, ServerUserBase> serverUserMap = serverUserService.getId2UserInfoMap();
		for (Integer userId : serverUserMap.keySet()) {
			ServerUserBase serverUser = serverUserMap.get(userId);

			// 创建用户相应的服务端业务请求接收限速器的条件：用户的连接类型为1或2（允许接收），用户的服务端接收限速大于0（要求限速）
			if ((serverUser.getConnectionType() == FrameworkConstants.CONNECTION_TYPE_ALL || serverUser
					.getConnectionType() == FrameworkConstants.CONNECTION_TYPE_MT)
					&& serverUser.getReceiveSpeedMax() > 0) {
				SecondOverFalseSpeed receiveSpeed = new SecondOverFalseSpeed();
				receiveSpeed.setSecondSpeed(serverUser.getReceiveSpeedMax());
				receiveSpeed.setSecondDivideNum(receiveSecondDivideNum);
				receiveSpeed.setFactor(receiveSpeedCountFactor);
				receiveSpeed.init();
				userId2ReceiveSpeedMap.put(userId, receiveSpeed);
			}

			// 创建用户相应的服务端业务请求发送限速器的条件：用户的连接类型为1或3（允许发送），用户的服务端发送限速大于0（要求限速）
			if ((serverUser.getConnectionType() == FrameworkConstants.CONNECTION_TYPE_ALL || serverUser
					.getConnectionType() == FrameworkConstants.CONNECTION_TYPE_MO) && serverUser.getSendSpeedMax() > 0) {
				SecondOverWaitSpeed sendSpeed = new SecondOverWaitSpeed();
				sendSpeed.setSecondSpeed(serverUser.getSendSpeedMax());
				sendSpeed.setSecondDivideNum(sendSecondDivideNum);
				sendSpeed.init();
				userId2SendSpeedMap.put(userId, sendSpeed);
			}
		}
	}

	/**
	 * 服务的销毁方法
	 */
	@PreDestroy
	public void destroy() {
		for (Integer userId : userId2ReceiveSpeedMap.keySet()) {
			SecondOverFalseSpeed receiveSpeed = userId2ReceiveSpeedMap.get(userId);
			receiveSpeed.destroy();
		}
		for (Integer userId : userId2SendSpeedMap.keySet()) {
			SecondOverWaitSpeed sendSpeed = userId2SendSpeedMap.get(userId);
			sendSpeed.destroy();
		}
	}

	@Override
	public IOverWaitSpeed getSendSpeedByUserId(Integer userId) {
		return userId2SendSpeedMap.get(userId);
	}

	@Override
	public IOverFalseSpeed getReceiveSpeedByUserId(Integer userId) {
		return userId2ReceiveSpeedMap.get(userId);
	}

}
