package sinova.tcp.framework.test;

/**
 * 测试命令操作接口
 * @author Timothy
 */
public interface ITestAction {

	/**
	 * 是否是应用停止命令
	 * @return boolean
	 */
	public boolean isFinish();

	/**
	 * 执行测试命令
	 * @param commandStr
	 */
	public void test(String commandStr);

}
