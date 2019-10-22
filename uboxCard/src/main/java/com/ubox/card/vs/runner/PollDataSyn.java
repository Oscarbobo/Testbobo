package com.ubox.card.vs.runner;

import com.ubox.card.util.SysUtil;
import com.ubox.card.util.logger.Logger;

/**
 * @author miral.gu
 * @date 2012-5-15 下午06:06:44
 */
public class PollDataSyn extends BaseSyn implements Runnable {
	/** 默认同步周期 */
	private static final int pollCycle = 3 * 60 * 1000;

	/** 心跳异常,再次唤起心跳时间 */
	private static final int restartTime = 5 * 60 * 1000;

	private static final PollDataSyn instance = new PollDataSyn();

	private PollDataSyn() {
	}

	public static PollDataSyn getInstance() {
		return instance;
	}

	@Override
	public void run() {
		restart: while (true) {// while(true)保证心跳线程一直存在
			SysUtil.sleep(pollCycle);
			try {
				Logger.info("PollDataSyn to vcardServer");
				doSyn();
			} catch (Exception e) {
				Logger.error(">>>> PollDataSyn data error.", e);
				SysUtil.sleep(restartTime);
				continue restart;// 心跳异常异常，再次唤起心跳
			}
		}
	}

}
