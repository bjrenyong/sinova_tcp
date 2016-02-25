package sinova.tcp.server.demo.service;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import sinova.tcp.framework.server.entity.ServerUserBase;
import sinova.tcp.framework.server.simple.AbsServerUserService;
import sinova.tcp.framework.server.simple.ServerUserInfo;

/**
 * 登录用户信息服务的Demo实现<br/>
 * 通过配置xml文件users_info.xml来注册用户的登录信息
 * @author Timothy
 */
@Service
public class DemoServerUserService extends AbsServerUserService {

	private static final Logger logger = LoggerFactory.getLogger(DemoServerUserService.class);
	private static final String FILENAME_USERS_INFO = "/users_info.xml";

	private Map<Integer, ServerUserBase> id2UserInfoMap;

	@PostConstruct
	public void init() {
		try {
			id2UserInfoMap = new HashMap<Integer, ServerUserBase>();
			URL url = this.getClass().getResource(FILENAME_USERS_INFO);
			SAXReader reader = new SAXReader();
			File file = new File(url.toURI());
			Document document = reader.read(file);
			Element root = document.getRootElement();
			List<Element> childElements = root.elements();
			for (Element child : childElements) {
				int userId = Integer.parseInt(child.attributeValue("userId"));
				String userName = child.attributeValue("userName");
				String password = child.attributeValue("password");
				String ip = child.attributeValue("ip");
				String secretKey = child.attributeValue("secretKey");
				int connectionType = Integer.parseInt(child.elementText("connectionType"));
				int moWindowSize = (child.elementText("moWindowSize") != null) ? Integer.parseInt(child
						.elementText("moWindowSize")) : 16;
				moWindowSize = (moWindowSize <= 0) ? 1 : moWindowSize;
				int sendSpeedMax = Integer.parseInt(child.elementText("sendSpeedMax"));
				int receiveSpeedMax = Integer.parseInt(child.elementText("receiveSpeedMax"));
				ServerUserInfo userInfo = new ServerUserInfo(userId, userName, password, ip, secretKey, connectionType,
						moWindowSize, sendSpeedMax, receiveSpeedMax);
				id2UserInfoMap.put(userId, userInfo);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public Map<Integer, ServerUserBase> getId2UserInfoMap() {
		return id2UserInfoMap;
	}

	@Override
	public ServerUserBase getServerUserByUserId(Integer userId) {
		return id2UserInfoMap.get(userId);
	}

	public static void main(String[] args) {
		DemoServerUserService userInfoService = new DemoServerUserService();
		userInfoService.init();
	}
}
