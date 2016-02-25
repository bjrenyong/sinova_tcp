package sinova.tcp.framework.test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件读取消息到队列生产者抽象类
 * @author Timothy
 * @param <T>
 */
public abstract class AbsFileToQueueProducer<T> implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(AbsFileToQueueProducer.class);

	/** 线程名，依赖注入 */
	private String threadName;
	/** 文件所在的源路径，依赖注入 */
	private String sourceDir;
	/** 文件处理完后搬移的目录路径，依赖注入 */
	private String targetDir;
	/** 线程运行标志 */
	private boolean runFlag = true;

	/** 同步锁 */
	protected byte[] lock = new byte[1];

	/**
	 * 从文件的一行中读取消息
	 * @param lineStr 文件的一行
	 * @return 消息
	 * @throws Exception
	 */
	protected abstract T readMsg(String lineStr) throws Exception;

	/**
	 * 发送消息到队列
	 * @param msg 消息
	 */
	protected abstract void sendMsgToQueue(T msg);

	/**
	 * 唤醒此生产线程
	 */
	protected void notifyService() {
		synchronized (lock) {
			lock.notify();
		}
	}

	/**
	 * 搬移文件到目标路径
	 * @param sourceFile 源文件
	 * @throws IOException
	 */
	private void moveFile(File sourceFile) throws IOException {
		File destFile = new File(targetDir + "/" + sourceFile.getName());
		if (destFile.exists()) {
			destFile.delete();
		}
		FileUtils.moveFile(sourceFile, destFile);
	}

	/**
	 * 线程休眠等待
	 * @param timeMillis 线程休眠等待的时间
	 */
	private void waitMillis(long timeMillis) {
		synchronized (lock) {
			try {
				lock.wait(timeMillis);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void run() {
		logger.info("{} thread begin!", threadName);
		File smsMtDirFile = new File(sourceDir);
		smsMtDirFile.mkdirs();
		new File(targetDir).mkdirs();
		String[] extensions = { "txt" };
		while (runFlag) {
			try {
				Collection<File> files = FileUtils.listFiles(smsMtDirFile, extensions, false);
				if (files == null || files.size() == 0) {
					logger.info("no files need to be processed!");
				} else {
					for (File file : files) {
						logger.info("process file {} begin!", file.getName());
						int sendCount = 0;
						LineIterator lineIterator = null;
						try {
							lineIterator = FileUtils.lineIterator(file);
							while (lineIterator.hasNext()) {
								T msg = readMsg(lineIterator.next());
								sendMsgToQueue(msg);
								sendCount++;
							}
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						} finally {
							LineIterator.closeQuietly(lineIterator);
						}
						logger.info("process file {} end! sendCount: {}", file.getName(), sendCount);
						moveFile(file);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			this.waitMillis(1000 * 60);
		}
		logger.info("{} thread end!", threadName);
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

	public void setRunFlag(boolean runFlag) {
		this.runFlag = runFlag;
	}

}
