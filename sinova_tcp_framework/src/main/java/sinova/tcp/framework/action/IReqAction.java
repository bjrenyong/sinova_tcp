package sinova.tcp.framework.action;

import sinova.tcp.framework.common.exception.BusinessException;
import sinova.tcp.protocol.IReq;
import sinova.tcp.protocol.IResp;

/**
 * 请求消息处理接口
 *
 * @param <T1> 请求消息
 * @param <T2> 响应消息
 */
public interface IReqAction<T1 extends IReq, T2 extends IResp> {

    /**
     * 执行请求操作
     *
     * @param request 请求消息
     * @param userId  消息所归属的用户ID
     * @return 对应的响应消息
     */
    public T2 action(T1 request, int userId) throws BusinessException;

}
