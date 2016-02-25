package sinova.tcp.framework.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 帮助命令操作类
 * @author Timothy
 */
@Service
public class HelpTestAction implements ITestAction{

	@Autowired
	private TestCommandService testCommandService;
	
	@Override
	public boolean isFinish() {
		return false;
	}

	@Override
	public void test(String commandStr) {
		testCommandService.printHelpMsg(System.err);
	}

}
