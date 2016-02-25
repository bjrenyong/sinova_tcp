package sinova.tcp.framework.common;

import java.util.Observable;

import org.springframework.stereotype.Service;

/**
 * TCP应用状态<br/>
 * 是可被观察类，状态一旦发生变化就会通知观察者<br/>
 * 目前应用状态不可逆，因此client端和server端使用同一个应用状态没有问题，否则需要进行改造
 * @author Timothy
 */
@Service
public class TcpAppStatus extends Observable {

	public static enum Status {
		STAUTS_INIT,
		STATUS_ACTIVE,
		STAUTS_CLOSING,
		STAUTS_CLOSED
	}

	private Status status = Status.STAUTS_INIT;

	public void setStatus(Status status) {
		this.status = status;
		this.setChanged();
		this.notifyObservers();
	}

	public Status getStatus() {
		return status;
	};

}
