package sinova.tcp.framework.test;

import org.springframework.stereotype.Service;

/**
 * 退出命令操作类
 * @author Timothy
 */
@Service
public class ExitTestAction implements ITestAction {

	@Override
	public boolean isFinish() {
		return true;
	}

	@Override
	public void test(String commandStr) {
		// do nothing
	}

}
