package com.ubox.card.device.bjszykt.threadwork;

import com.ubox.card.device.bjszykt.localwork.LocWorker;
import com.ubox.card.device.bjszykt.network.paramdownload.DownloadWorker;
import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.InitWoker;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.device.bjszykt.pubwork.PubWorker;
import com.ubox.card.util.logger.Logger;
import com.ubox.card.core.WorkPool;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BJSZYKTthreadworker {

    private static final ScheduledThreadPoolExecutor schService = new ScheduledThreadPoolExecutor(5);

    private static final Runnable initTask = new Runnable() {
    	@Override
    	public void run() {
    		/** 系统启动,执行 "批上送+签退+签到"任务,最多签到3次 */
    		int signTimes = 0;
    		while(signTimes < 3) {
    			PubWorker.workSleep(10000);	
    			
    			if (runInit() == 0) {
    				Logger.info("SUCCESS: settle success");
    				break;
    			} else {
    				signTimes ++;
    				Logger.warn("WARN: settle fail.signTimes=" + signTimes);
    			}
    		}
    		
    		InitWoker.paramsContextInit(); // 工作参数环境初始化
            LocalContext.VCARD_STATUS = LocalContext.VCRAD_READY;

    		/** 签到成功, 定时执行签退签到*/
    		long delay = PubUtils.getLimitTime() - System.currentTimeMillis();
    		if(delay <= 0) {
    			Logger.warn(">>>> WARN: LimitTime exceptoin.LimitTime=" + PubUtils.getFromConfigs("LimitTime"));
    			delay = System.currentTimeMillis() + 86400000;
    		}

    		schService.scheduleWithFixedDelay(new Runnable() {
    			@Override
    			public void run() {
    				LocalContext.VCARD_STATUS = LocalContext.SCHEDULE_WORK;
    				PubWorker.workSleep(30000); // 休眠30s, 使定时任务不与交易并发
    				if (runInit() <= 2) {
    					Logger.info("SUCCESS: schedule success");
    				} else {
    					Logger.warn("schedule fail");
    				}
    				
    				LocalContext.VCARD_STATUS = LocalContext.VCRAD_READY;
    			}

    		}, delay, 86400000, TimeUnit.MILLISECONDS); // 每6小时签退签到一次

    		/** 数据文件上传, 每3分钟上传销售数据*/
    		schService.scheduleWithFixedDelay(new Runnable() {
    			@Override
    			public void run() { LocWorker.uploadTransData(); }
    		}, 3L, 3L, TimeUnit.MINUTES);
    	}
    };
    
    public static void threadworkStart() { WorkPool.executeTask(initTask); }

    /**
     * 执行刷卡初始化工作
     *
     * @return 0-成功,非0失败
     */
    private static int runInit() {
        LocalContext.VCARD_STATUS = LocalContext.SIGN_WORK;

        /** 本地参数初始化 */
        if(InitWoker.localContextInit() != 0) {
            Logger.warn(">>>> FAIL: Local context init");
            return 1;
        }
        Logger.info(">>>> SUCCESS:Local context init");

        /** 批上送 + 签退.若交易数据不存在,则签退 */
        if(InitWoker.uploadAndSignOut() != 0) {
            Logger.warn(">>>> FAIL:batch upload AND sign out");
            return 2;
        }
        Logger.info(">>>> SUCCESS:batch upload AND sign out");

        /** 判断本地参数文件, 根据判断结果进行参数下载 */
        if(InitWoker.checkLocalContext() == 0)
            Logger.info(">>>> SUCCESS: Local context information integrity");
        else {
            Logger.warn(">>>> WARN: Local context information missing");
            //参数查询,进行缺失参数下载
            int paramsFlag = InitWoker.paramsQuery(0);
            if(paramsFlag == -1) return 3; // 参数查询失败

            List<DownloadWorker> downList = InitWoker.parseDownLoadparams(paramsFlag);
            for(DownloadWorker worker : downList) {
                if(worker.downloadWork() != 0) {
                    Logger.warn(">>>> FAIL:Param download fail.");
                    return 4;
                }
            }
        }

        /** 设备进行签到 */
        if(InitWoker.signInWork() != 0) {
            Logger.warn(">>>> FAIL:sign in");
            return 5;
        }
        
        Logger.info(">>>> SUCCESS: sign in");

        return 0;
    }

}

