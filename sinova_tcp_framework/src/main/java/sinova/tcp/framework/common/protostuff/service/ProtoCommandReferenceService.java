package sinova.tcp.framework.common.protostuff.service;

import io.protostuff.Schema;

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
import sinova.tcp.framework.common.protostuff.config.ProtoReqCommandReference;
import sinova.tcp.framework.common.protostuff.config.ProtoRespCommandReference;
import sinova.tcp.framework.common.service.ICommandReferenceService;
import sinova.tcp.protocol.IMsg;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 命令关联信息服务类--protostuff版<br/>
 * 与标准版的区别：<br/>
 * 1. 依赖于ProtoReqCommandReference，而标准版依赖于ReqCommandReference<br/>
 * 2. 增加方法：getCommandId2SchemaMap， 获取命令ID到schema映射集合<br/>
 * 3. 增加方法：getClass2SchemaMap，获取消息类到schema映射集合
 * @author Timothy
 */
@Service
public class ProtoCommandReferenceService implements ICommandReferenceService {

	/** 请求命令关联数据集合，依赖注入 */
	@Resource
	private List<ProtoReqCommandReference<? extends IReq, ? extends IResp>> reqCommandReferenceList;
	/** 响应命令关联数据集合，依赖注入 */
	@Resource
	private List<ProtoRespCommandReference<? extends IReq, ? extends IResp>> respCommandReferenceList;

	/** 命令ID到消息类映射集合 */
	private Map<Short, Class<? extends IMsg>> cid2ClassMap;
	/** 消息类到命令ID映射集合 */
	private Map<Class<? extends IMsg>, Short> class2CidMap;
	/** 请求消息类到请求命令关联类映射集合 */
	private Map<Class<? extends IReq>, ProtoReqCommandReference<? extends IReq, ? extends IResp>> class2ReqReferenceMap;
	/** 响应消息类到响应命令关联类映射集合 */
	private Map<Class<? extends IResp>, ProtoRespCommandReference<? extends IReq, ? extends IResp>> class2RespReferenceMap;
	/** 命令ID到schema映射集合 */
	private Map<Short, Schema<? extends IMsg>> cid2SchemaMap;
	/** 消息类到schema映射集合 */
	private Map<Class<? extends IMsg>, Schema<? extends IMsg>> class2SchemaMap;

	@PostConstruct
	public void init() {
		// 初始化
		cid2ClassMap = new HashMap<Short, Class<? extends IMsg>>();
		class2CidMap = new HashMap<Class<? extends IMsg>, Short>();
		class2ReqReferenceMap = new HashMap<Class<? extends IReq>, ProtoReqCommandReference<? extends IReq, ? extends IResp>>();
		class2RespReferenceMap = new HashMap<Class<? extends IResp>, ProtoRespCommandReference<? extends IReq, ? extends IResp>>();
		cid2SchemaMap = new HashMap<Short, Schema<? extends IMsg>>();
		class2SchemaMap = new HashMap<Class<? extends IMsg>, Schema<? extends IMsg>>();
		// 循环请求命令关联数据集合
		for (ProtoReqCommandReference<? extends IReq, ? extends IResp> reqCommandReference : reqCommandReferenceList) {
			cid2ClassMap.put(reqCommandReference.getCommandId(), reqCommandReference.getCommandClass());
			class2CidMap.put(reqCommandReference.getCommandClass(), reqCommandReference.getCommandId());
			class2ReqReferenceMap.put(reqCommandReference.getCommandClass(), reqCommandReference);
			cid2SchemaMap.put(reqCommandReference.getCommandId(), reqCommandReference.getReqSchema());
			class2SchemaMap.put(reqCommandReference.getCommandClass(), reqCommandReference.getReqSchema());
		}
		// 循环响应命令关联数据集合
		for (ProtoRespCommandReference<? extends IReq, ? extends IResp> respCommandReference : respCommandReferenceList) {
			cid2ClassMap.put(respCommandReference.getCommandId(), respCommandReference.getCommandClass());
			class2CidMap.put(respCommandReference.getCommandClass(), respCommandReference.getCommandId());
			class2RespReferenceMap.put(respCommandReference.getCommandClass(), respCommandReference);
			cid2SchemaMap.put(respCommandReference.getCommandId(), respCommandReference.getRespSchema());
			class2SchemaMap.put(respCommandReference.getCommandClass(), respCommandReference.getRespSchema());
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

	/** 获取命令ID到schema映射集合 */
	public Map<Short, Schema<? extends IMsg>> getCommandId2SchemaMap() {
		return cid2SchemaMap;
	}

	/** 获取消息类到schema映射集合 */
	public Map<Class<? extends IMsg>, Schema<? extends IMsg>> getClass2SchemaMap() {
		return class2SchemaMap;
	}

}
