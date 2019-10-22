package com.ubox.card.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ubox.card.util.logger.Logger;

public class WorkPool {

    /** 执行线程池  **/
    private static final ExecutorService exe = Executors.newFixedThreadPool(25);
    
    public static void executeTask(Runnable run) {
    	try { 
    		exe.execute(run); 
    	} catch(Exception e) {
    		Logger.error(">>>>ERROR: execute task fail", e);
    	}
    }
    
}
