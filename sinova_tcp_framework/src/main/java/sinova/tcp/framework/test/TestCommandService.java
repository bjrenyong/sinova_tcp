package sinova.tcp.framework.test;

import java.io.PrintStream;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

/**
 * 测试命令服务
 * @author Timothy
 */
@Service
public class TestCommandService {

	@Resource
	private List<TestCommand> testCommands;

	/** 测试命令的最大长度，用于帮助信息的格式化 */
	private int commandMaxLen = 0;
	private String outFormat0;
	private String outFormat1;

	@PostConstruct
	public void init() {
		for (TestCommand testCommand : testCommands) {
			commandMaxLen = Math.max(commandMaxLen, testCommand.getCommand().length());
		}
		this.outFormat0 = " %1$-" + commandMaxLen + "s : %2$-1s";
		this.outFormat1 = " %1$-" + commandMaxLen + "s   %2$-1s";
	}

	public TestCommand matchTestCommand(String commandStr) {
		for (TestCommand testCommand : testCommands) {
			if (commandStr.trim().matches(testCommand.getCommandRegex())) {
				return testCommand;
			}
		}
		return null;
	}

	public void printHelpMsg(PrintStream out) {
		for (TestCommand testCommand : testCommands) {
			String[] lineArr = testCommand.getUseage().split("\n");
			for (int i = 0; i < lineArr.length; i++) {
				if (i == 0) {
					String arg0 = testCommand.getCommand();
					String arg1 = lineArr[0];
					String outLine = String.format(this.outFormat0, arg0, arg1);
					out.println(outLine);
				} else {
					String arg0 = "";
					String arg1 = lineArr[i];
					String outLine = String.format(this.outFormat1, arg0, arg1);
					out.println(outLine);
				}
			}
		}
	}
	
//	public void nettyClose(){
//		
//	}
}
