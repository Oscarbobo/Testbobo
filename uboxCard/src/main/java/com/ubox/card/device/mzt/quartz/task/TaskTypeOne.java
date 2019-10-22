package com.ubox.card.device.mzt.quartz.task;

import java.util.concurrent.TimeUnit;

import com.ubox.card.device.mzt.BlacklistManager;

public class TaskTypeOne extends TaskBase {
	
	private static final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;
	
	private final long executeTime;
	
	public TaskTypeOne(long executeTime) {
		this.executeTime = executeTime;
	}
	
	@Override
	public void start() {
		long initialDelay;
		long timeDiff = executeTime - System.currentTimeMillis();
		
		if(timeDiff > 0) {
			initialDelay = timeDiff;
		} else {
			initialDelay = ONE_DAY_MILLIS + timeDiff;
		}
		
		TaskBase.sheduledExecutor.scheduleWithFixedDelay(
				new BlacklistManager.DownloadTask(), 
				initialDelay, 
				60 * 24, // 每隔24小时执行一次
				TimeUnit.MINUTES
		);
	}
}
