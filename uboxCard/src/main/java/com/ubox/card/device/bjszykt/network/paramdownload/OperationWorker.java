package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.util.logger.Logger;

import java.util.HashMap;

public class OperationWorker extends DownloadWorker {

	public static int agentShortCode;
	public static byte[] mchnitid = new byte[6];
    public OperationWorker() {
        super(LocalContext.OPERATION_PARAM, 165, "Operation", new byte[]{(byte) 0x04, 0x00, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {//机具运营参数
        try {
            byte[]  splitUnitCode       = new byte[4];
            byte[]  sendUnitCode        = new byte[4];
            byte[]  receUnitCode        = new byte[4];
            byte[]  unitName            = new byte[40];
            byte[]  nodeName            = new byte[40];
            byte[]  onlineThreshold     = new byte[4];
            byte[]  dailUserName_1      = new byte[10];
            byte[]  dailUserPwd_1       = new byte[10];
            byte[]  dailUserName_2      = new byte[10];
            byte[]  dailUserPwd_2       = new byte[10];
            byte[]  dailUserName_3      = new byte[10];
            byte[]  dailUserPwd_3       = new byte[10];
            int     dataUploadMode      ;
            int     refundWay           ;

            System.arraycopy(parmData,       0,      splitUnitCode,      0,      4);
            System.arraycopy(parmData,       4,      sendUnitCode,       0,      4);
            System.arraycopy(parmData,       8,      receUnitCode,       0,      4);
            agentShortCode = parmData[12];
            System.arraycopy(parmData,       13,     mchnitid,             0,      6);
            System.arraycopy(parmData,       19,     unitName,           0,      40);
            System.arraycopy(parmData,       59,     nodeName,           0,      40);
            System.arraycopy(parmData,       99,     onlineThreshold,    0,      4);
            System.arraycopy(parmData,       103,    dailUserName_1,     0,      10);
            System.arraycopy(parmData,       113,    dailUserPwd_1,      0,      10);
            System.arraycopy(parmData,       123,    dailUserName_2,     0,      10);
            System.arraycopy(parmData,       133,    dailUserPwd_2,      0,      10);
            System.arraycopy(parmData,       143,    dailUserName_3,     0,      10);
            System.arraycopy(parmData,       153,    dailUserPwd_3,      0,      10);
            dataUploadMode = parmData[163];
            refundWay      = parmData[164];

            Logger.info(
                    "\n>>>> Operation <<<<" +
                    "\n>>>> splitUnitCode  :     " + PubUtils.BA2HS(splitUnitCode) +
                    "\n>>>> sendUnitCode   :     " + PubUtils.BA2HS(sendUnitCode) +
                    "\n>>>> receUnitCode   :     " + PubUtils.BA2HS(receUnitCode) +
                    "\n>>>> agentShortCode :     0x" + PubUtils.BA2HS(new byte[]{(byte) agentShortCode}) +
                    "\n>>>> mchnitid       :     " + PubUtils.BA2HS(mchnitid) +
                    "\n>>>> unitName       :     " + new String(unitName, "GBK") +
                    "\n>>>> nodeName       :     " + new String(nodeName, "GBK") +
                    "\n>>>> onlineThreshold:     0x" + PubUtils.BA2HS(onlineThreshold) + " = " + PubUtils.b2iLt(onlineThreshold, 4) +
                    "\n>>>> dailUserName 1 :     " + new String(dailUserName_1) +
                    "\n>>>> dailUserPwd  1 :     " + new String(dailUserPwd_1) +
                    "\n>>>> dailUserName 2 :     " + new String(dailUserName_2) +
                    "\n>>>> dailUserPwd  2 :     " + new String(dailUserPwd_2) +
                    "\n>>>> dailUserName 3 :     " + new String(dailUserName_3) +
                    "\n>>>> dailUserPwd  3 :     " + new String(dailUserPwd_3) +
                    "\n>>>> dataUploadMode :     0x" + PubUtils.BA2HS(new byte[]{(byte) dataUploadMode}) +
                    "\n>>>> refundWay      :     0x" + PubUtils.BA2HS(new byte[]{(byte) refundWay})
            );

            /*持久化splitUnitCode,sendUnitCode,receUnitCode*/
            String splitUnitCodePersit  = PubUtils.BA2HS(splitUnitCode);
            String sendUnitCodePersit   = PubUtils.BA2HS(sendUnitCode);
            String receUnitCodePersit   = PubUtils.BA2HS(receUnitCode);

            HashMap<String, String> kvs = new HashMap<String, String>();
            kvs.put("splitUnitCode", splitUnitCodePersit);
            kvs.put("sendUnitCode" , sendUnitCodePersit);
            kvs.put("receUnitCode" , receUnitCodePersit);

            if(PubUtils.configsPerisit(kvs) == 0) {
                Logger.info(
                        "\n>>>> SUCCESS:Persist [splitUnitCode],[endUnitCode],[receUnitCode] <<<<" +
                        "\n>>>> splitUnitCode  :" + splitUnitCodePersit +
                        "\n>>>> sendUnitCode   :" + sendUnitCodePersit  +
                        "\n>>>> receUnitCode   :" + receUnitCodePersit
                );
            }else {
                Logger.warn(">>>> FAIL:Persist [splitUnitCode],[endUnitCode],[receUnitCode] <<<<");
            }
        } catch(Exception e) {
            Logger.warn(">>>> FAIL:Analyze Operation");
        }
    }
}
