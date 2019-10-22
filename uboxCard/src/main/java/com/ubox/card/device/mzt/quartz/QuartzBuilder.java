package com.ubox.card.device.mzt.quartz;

import java.util.Calendar;

import com.ubox.card.device.mzt.quartz.task.TaskTypeOne;

public class QuartzBuilder {
	
	public static QuartzTask builde(int taskType, Runnable runTask) {
		Calendar calendar = Calendar.getInstance();
		
		if(1 == taskType) {
			calendar.set(Calendar.HOUR_OF_DAY, 5);
			calendar.set(Calendar.MINUTE, 30);
			// 05:30执行任务
			return new TaskTypeOne(calendar.getTimeInMillis());
		} else if(2 == taskType) {
			calendar.set(Calendar.HOUR_OF_DAY, 10);
			calendar.set(Calendar.MINUTE, 30);
			// 10:30执行任务
			return new TaskTypeOne(calendar.getTimeInMillis());
		} else {
			return new TaskTypeOne(System.currentTimeMillis() + 20 * 1000);
		}
	}
}
