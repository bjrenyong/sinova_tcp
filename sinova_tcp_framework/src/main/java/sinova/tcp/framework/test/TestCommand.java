package sinova.tcp.framework.test;

/**
 * 测试用命令类
 * @author Timothy
 */
public class TestCommand {

	/** 命令 */
	private String command;
	/** 匹配命令的正则表达式 */
	private String commandRegex;
	/** 命令说明 */
	private String useage;
	/** 命令操作 */
	private ITestAction testAction;

	public TestCommand(String command, String commandRegex, String useage, ITestAction testAction) {
		this.command = command;
		this.commandRegex = commandRegex;
		this.useage = useage;
		this.testAction = testAction;
	}

	public String getCommand() {
		return command;
	}

	public String getCommandRegex() {
		return commandRegex;
	}

	public String getUseage() {
		return useage;
	}

	public ITestAction getTestAction() {
		return testAction;
	}

}
