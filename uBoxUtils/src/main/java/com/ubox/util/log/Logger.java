package com.ubox.util.log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;



import com.ubox.util.UboxLog;

public class Logger {
	
	private static final int  capacity_size    = 100;
	private static ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<String>(capacity_size,true);
	private static final int TIMEOUT = 200;
	private static final String  WARN  = "warn | ";
	private static final String  ERROR = "error | ";
	private static final String  INFO  = "info | ";
	private static final String  separator = "_SEPARATOR_";
	static class LogcatThread extends Thread{
		@Override
		public void run() {
			while (true) {
				try {
					String logInfo = abq.poll(600, TimeUnit.MILLISECONDS);
					if(logInfo != null){
						String[] split = logInfo.split(separator);
						if(split.length == 2){
							UboxLog.get().log(split[0],split[1]);
						}
					}
				} catch (InterruptedException e) {      
					e.printStackTrace();
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
	}
	public static void info(String tag,String msg) {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
		StringBuffer toStringBuffer = new StringBuffer("[").append(INFO).append(
        traceElement.getFileName()).append(" | ").append(
        traceElement.getLineNumber()).append(" | ").append(
        traceElement.getMethodName()).append("()").append("]  ").append(msg);
		putMessage(tag+separator+toStringBuffer.toString());
	}
	
	public static void warn(String tag,String msg) {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
		StringBuffer toStringBuffer = new StringBuffer("[").append(WARN).append(
        traceElement.getFileName()).append(" | ").append(
        traceElement.getLineNumber()).append(" | ").append(
        traceElement.getMethodName()).append("()").append("] ").append(msg);
		putMessage(tag+separator+toStringBuffer.toString());
	}
	
	public static void error(String tag,String msg) {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
		StringBuffer toStringBuffer = new StringBuffer("[").append(ERROR).append(
        traceElement.getFileName()).append(" | ").append(
        traceElement.getLineNumber()).append(" | ").append(
        traceElement.getMethodName()).append("()").append("]  ").append(msg);
		putMessage(tag+separator+toStringBuffer.toString());
	}
	
	private synchronized static void startLogcat(){
		if(logcatThread == null || !logcatThread.isAlive()){
			if(logcatThread != null){logcatThread.interrupt();}
			logcatThread = new LogcatThread();
			logcatThread.start();
		}
	}
	private static void putMessage(String logInfo){
		try {
			abq.offer(logInfo, TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(logcatThread == null || !logcatThread.isAlive()){
			startLogcat();
		}
	}
	private static LogcatThread logcatThread = null;
}
