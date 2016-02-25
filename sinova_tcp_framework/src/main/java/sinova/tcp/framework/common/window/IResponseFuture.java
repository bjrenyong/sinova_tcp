package sinova.tcp.framework.common.window;

import sinova.tcp.framework.common.protocol.TransportMsg;

public interface IResponseFuture {

	/**
	 * get result.
	 * @return result.
	 */
	TransportMsg get();

	/**
	 * get result with the specified timeout.
	 * @param timeoutInMillis timeout.
	 * @return result.
	 */
	TransportMsg get(int timeoutInMillis);

	/**
	 * check is done.
	 * @return done or not.
	 */
	boolean isDone();

}