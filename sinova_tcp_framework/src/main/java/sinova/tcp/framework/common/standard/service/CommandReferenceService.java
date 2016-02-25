package sinova.tcp.framework.common.standard.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import sinova.tcp.framework.action.IReqAction;
import sinova.tcp.framework.action.IRespAction;
import sinova.tcp.framework.common.config.ReqCommandReference;
import sinova.tcp.framework.common.config.RespCommandReference;
import sinova.tcp.framework.common.service.ICommandReferenceService;
import sinova.tcp.protocol.IMsg;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 命令关联信息服务类--标准版<br/>
 * 注意：标准版和protostuff版互斥<br/>
 * 此类提供了命令关联信息的相关方法
 * @author Timothy
 */
@Service
public class CommandReferenceService implements ICommandReferenceService {

	/** 请求命令关联数据集合，依赖注入 */
	@Resource
	private List<ReqCommandReference<? extends IReq, ? extends IResp>> reqCommandReferenceList;
	/** 响应命令关联数据集合，依赖注入 */
	@Resource
	private List<RespCommandReference<? extends IReq, ? extends IResp>> respCommandReferenceList;

	/** 命令ID到消息类映射集合 */
	private Map<Short, Class<? extends IMsg>> cid2ClassMap;
	/** 消息类到命令ID映射集合 */
	private Map<Class<? extends IMsg>, Short> class2CidMap;
	/** 请求消息类到请求命令关联类映射集合 */
	private Map<Class<? extends IReq>, ReqCommandReference<? extends IReq, ? extends IResp>> class2ReqReferenceMap;
	/** 响应消息类到响应命令关联类映射集合 */
	private Map<Class<? extends IResp>, RespCommandReference<? extends IReq, ? extends IResp>> class2RespReferenceMap;

	@PostConstruct
	public void init() {
		// 初始化
		cid2ClassMap = new HashMap<Short, Class<? extends IMsg>>();
		class2CidMap = new HashMap<Class<? extends IMsg>, Short>();
		class2ReqReferenceMap = new HashMap<Class<? extends IReq>, ReqCommandReference<? extends IReq, ? extends IResp>>();
		class2RespReferenceMap = new HashMap<Class<? extends IResp>, RespCommandReference<? extends IReq, ? extends IResp>>();
		// 循环请求命令关联数据集合
		for (ReqCommandReference<? extends IReq, ? extends IResp> reqCommandReference : reqCommandReferenceList) {
			cid2ClassMap.put(reqCommandReference.getCommandId(), reqCommandReference.getCommandClass());
			class2CidMap.put(reqCommandReference.getCommandClass(), reqCommandReference.getCommandId());
			class2ReqReferenceMap.put(reqCommandReference.getCommandClass(), reqCommandReference);
		}
		// 循环响应命令关联数据集合
		for (RespCommandReference<? extends IReq, ? extends IResp> respCommandReference : respCommandReferenceList) {
			cid2ClassMap.put(respCommandReference.getCommandId(), respCommandReference.getCommandClass());
			class2CidMap.put(respCommandReference.getCommandClass(), respCommandReference.getCommandId());
			class2RespReferenceMap.put(respCommandReference.getCommandClass(), respCommandReference);
		}
	}

	@Override
	public Class<? extends IMsg> getMsgClassByCommandId(Short commandId) {
		return cid2ClassMap.get(commandId);
	}

	@Override
	public Short getCommandIdByMsgClass(Class<? extends IMsg> msgClass) {
		return class2CidMap.get(msgClass);
	}

	@Override
	public IReqAction<? extends IReq, ? extends IResp> getReqActionByReqClass(Class<? extends IReq> reqClass) {
		ReqCommandReference<? extends IReq, ? extends IResp> reference = class2ReqReferenceMap.get(reqClass);
		if (reference != null) {
			return reference.getReqAction();
		} else {
			return null;
		}
	}

	@Override
	public boolean reqHasReference(IReq reqMsg) {
		if (class2ReqReferenceMap.containsKey(reqMsg.getClass())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public IRespAction<? extends IReq, ? extends IResp> getRespActionByRespClass(Class<? extends IResp> respClass) {
		RespCommandReference<? extends IReq, ? extends IResp> reference = class2RespReferenceMap.get(respClass);
		if (reference != null) {
			return reference.getRespAction();
		} else {
			return null;
		}
	}

	@Override
	public Short getRespCommandIdByRespClass(Class<? extends IResp> respClass) {
		RespCommandReference<? extends IReq, ? extends IResp> reference = class2RespReferenceMap.get(respClass);
		if (reference != null) {
			return reference.getCommandId();
		} else {
			return null;
		}
	}
}
