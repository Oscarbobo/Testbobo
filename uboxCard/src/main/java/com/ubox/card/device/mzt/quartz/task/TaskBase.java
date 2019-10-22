package com.ubox.card.device.mzt.quartz.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.ubox.card.device.mzt.quartz.QuartzTask;

public abstract class TaskBase implements QuartzTask{
	
	protected static final ScheduledExecutorService sheduledExecutor = Executors.newScheduledThreadPool(5);

}
