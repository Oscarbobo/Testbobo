package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.network.NetWorker;
import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.device.bjszykt.pubwork.Result;
import com.ubox.card.util.logger.Logger;

public abstract class DownloadWorker {

    private static final int SUCCESS    = 0;
    private static final int FAIL       = 1;

    protected String    persitFileName;
    protected int       dataUnitLength;
    protected String    paramName;
    protected byte[]    paramCode;

    protected DownloadWorker(String persitFileName, int dataUnitLength, String paramName, byte[] paramCode) {
        this.persitFileName = persitFileName;
        this.dataUnitLength = dataUnitLength;
        this.paramName      = paramName;
        this.paramCode      = paramCode;
    }

    /**
     * 参数下载工作
     *
     * @return 0-成功;非0-失败
     */
    public final int downloadWork() {
        Result result = NetWorker.paramDownloadApply(paramCode);

        if(result.codeType != Result.CODESUCCESS) {
            Logger.info(">>>> FAIL:Download [" + paramName + "]");
            return FAIL;
        }

        int persitStatus = persitWork(result.fdBytes);
        if(persitStatus != 0) {
            Logger.warn(">>>> FAIL: Persist [" + paramName + "]");
            return FAIL;
        } else {
            Logger.info(">>>> SUCCESS: Persist [" + paramName+ "]");
            return SUCCESS;
        }
    }

    /**
     * 解析参数数据
     *
     * @param parmData 参数数据
     */
    protected abstract void analyisData(byte[] parmData);

    /**
     * 参数数据持久化工作
     *
     * @param paramData 参数数据
     * @return 0-持久化成功;非0-持久化失败
     */
    private int persitWork(byte[] paramData) {
        String          blackListFilePath   = LocalContext.workPath + java.io.File.separator + persitFileName;
        java.io.File    blackListFile       = PubUtils.fileGetOrCreate(blackListFilePath, persitFileName);

        if(blackListFile == null) return 1;

        analyisData(paramData);
        return PubUtils.writeData2File(blackListFile, false, PubUtils.BA2HS(paramData), dataUnitLength);
    }

}
