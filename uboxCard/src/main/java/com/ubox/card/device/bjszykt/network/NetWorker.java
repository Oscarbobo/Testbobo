package com.ubox.card.device.bjszykt.network;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.device.bjszykt.pubwork.PubWorker;
import com.ubox.card.device.bjszykt.pubwork.Result;
import com.ubox.card.device.bjszykt.recharge.RechargeInfo;
import com.ubox.card.device.bjszykt.recharge.recharge;
import com.ubox.card.device.bjszykt.server.bean.KCQTradeResponse;
import com.ubox.card.util.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class NetWorker {

    /**
     * 签到申请
     *
     * @return 反馈结果
     */
    public static Result signApply() {
        /** 初始化数据报文 **/
        byte[]  messageType = PubUtils.HS2BA("5001");
        byte    ver         = 0x02;
        byte[]  sysDateTime = PubUtils.HS2BA(PubUtils.generateSysTime());
        byte[]  oprId       = PubUtils.HS2BA(LocalContext.oprId);
        byte[]  POSID       = NetUtils.generatePOSID(LocalContext.posId);
        byte[]  ISAM        = PubUtils.HS2BA(LocalContext.CACHE_SAM);
        byte[]  unitId      = PubUtils.HS2BA(LocalContext.unitId);
        byte[]  mchntId     = PubUtils.HS2BA(LocalContext.mchntId);
        byte    programName = Byte.parseByte(LocalContext.programName);
        byte[]  random      = PubUtils.HS2BA(LocalContext.CACHE_RANDOM); // 此处是广义BCD码,取值范围0~F

        String  POS_IC_SEQ =  PubUtils.getFromConfigs("POS_IC_SEQ");//IC_SEQ值获取
        byte[]  posIcSeq    = PubUtils.HS2BA(POS_IC_SEQ); // 签退需要维护IC_SEQ
        byte[]  posAccSeq   = new byte[]{0x00, 0x00, 0x00, 0x00};
        byte[]  posCommSeq  = new byte[]{0x00, 0x00, 0x00, 0x00};

        byte[] data = new byte[67];
        System.arraycopy(sysDateTime,   0, data, 0 , 7);
        System.arraycopy(oprId,         0, data, 7 , 3);
        System.arraycopy(POSID,         0, data, 10, 20);
        System.arraycopy(ISAM,          0, data, 30, 6);
        System.arraycopy(unitId,        0, data, 36, 4);
        System.arraycopy(mchntId,       0, data, 40, 6);
        data[46] = programName;
        System.arraycopy(random,        0, data, 47, 8);
        System.arraycopy(posIcSeq,      0, data, 55, 4);
        System.arraycopy(posAccSeq,     0, data, 59, 4);
        System.arraycopy(posCommSeq,    0, data, 63, 4);

        /** 命令处理 **/
        Result result = commandWork(NetUtils.initMessage(messageType, ver, data), LocalContext.URL_SIGN);
        if(result.codeType != Result.CODESUCCESS) {
            Logger.info(">>>>FAIL: sign apply");
            return result;
        }

        /** 判断应答码 **/
        byte responseCode = result.fdBytes[22];// 消息报文的应答码的位置(不同的应答报文,应答码位置不同)
        if(responseCode != 0x00){
            Logger.info(">>>>FAIL: Sign apply response.responseCode=0x" + PubUtils.BA2HS(new byte[]{responseCode}));
            return new Result(Result.NETCODE, result.fdBytes, -1 , null , responseCode);
        }

        return result;
    }


    /**
     * 预充值订单查询申请/应答
     */
    public static Result rechargeSearch(KCQTradeResponse bean) {/** 初始化数据报文 **/
        Result result = null;
        try {
            Logger.info("5141 data :::::: "+PubUtils.generateSysTime());
            Logger.info(""+RechargeInfo.oprId);
            Logger.info(""+RechargeInfo.posId);
            Logger.info(""+RechargeInfo.sam);
            Logger.info(""+RechargeInfo.unitId);
            Logger.info(""+RechargeInfo.mchntId);
            Logger.info(""+RechargeInfo.batchNO);
            Logger.info(""+RechargeInfo.cardNo);
            Logger.info(""+RechargeInfo.cardType);
            Logger.info(""+RechargeInfo.cardPhyType);
            Logger.info(""+RechargeInfo.befBal);
            byte[] messageType = PubUtils.HS2BA("5141");
            byte ver = 0x01;
            byte[] sysDateTime = PubUtils.HS2BA(PubUtils.generateSysTime());
            byte[] oprId = PubUtils.HS2BA(RechargeInfo.oprId);
            byte[] posId = NetUtils.generatePOSID(RechargeInfo.posId);
            byte[] iSam = PubUtils.HS2BA(RechargeInfo.sam);
            byte[] unitId = PubUtils.HS2BA(RechargeInfo.unitId);
            byte[] mchntId = PubUtils.HS2BA(LocalContext.mchntId);
            byte[] batchNo = PubUtils.i2bLt(Integer.valueOf(PubUtils.getFromConfigs("batchNO")), 4);
            byte[] cardNo = PubUtils.HS2BA(RechargeInfo.cardNo);
            byte[] cardType = PubUtils.HS2BA(RechargeInfo.cardType);
            byte[] cardPhytype = PubUtils.HS2BA(RechargeInfo.cardPhyType);
            byte[] befBal = PubUtils.HS2BA(RechargeInfo.befBal);

            byte[] data = new byte[64];
            System.arraycopy(sysDateTime,   0, data,  0, 7);
            System.arraycopy(oprId,         0, data,  7, 3);
            System.arraycopy(posId,         0, data, 10, 20);
            System.arraycopy(iSam,          0, data, 30, 6);
            System.arraycopy(unitId,        0, data, 36, 4);
            System.arraycopy(mchntId,       0, data, 40, 6);
            System.arraycopy(batchNo,       0, data, 46, 4);
            System.arraycopy(cardNo,        0, data, 50, 8);
            System.arraycopy(cardType,      0, data, 58, 1);
            System.arraycopy(cardPhytype,   0, data, 59, 1);
            System.arraycopy(befBal, 0, data, 60, 4);

            // 命令处理
            result = commandWork(NetUtils.initMessage(messageType, ver, data), RechargeInfo.URL_RECHARGE_APPLY, RechargeInfo.msg1, bean);
            if(result.codeType != Result.CODESUCCESS) {
                Logger.info(">>>>FAIL: rechargeSearch");
                return result;
            }

            // 判断应答码
            byte responseCode = result.fdBytes[48];// 消息报文的应答码的位置(不同的应答报文,应答码位置不同)
            if(responseCode != 0x00){
                Logger.info(">>>>FAIL  5141  : rechargeSearch response.responseCode=0x" + PubUtils.BA2HS(new byte[]{responseCode}));
                return new Result(Result.NETCODE, result.fdBytes, -1 , PubUtils.BA2HS(new byte[]{responseCode}) , responseCode);
            }
        }catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }

        return result;

    }

    /**
     * 充值申请应答
     */
    public static Result rechargeApply(KCQTradeResponse bean) {
        Result result = null;
        try {

            /** 初始化数据报文 **/
            byte[] messageType = PubUtils.HS2BA("5143");
            byte ver = 0x01;
            byte[] sysDateTime = PubUtils.HS2BA(PubUtils.generateSysTime());
            byte[] oprId = PubUtils.HS2BA(RechargeInfo.oprId);
            byte[] posId = NetUtils.generatePOSID(RechargeInfo.posId);
            byte[] iSam = PubUtils.HS2BA(RechargeInfo.sam);
            byte[] unitId = PubUtils.HS2BA(RechargeInfo.unitId);
            byte[] mchntId = PubUtils.HS2BA(LocalContext.mchntId);
            byte[] batchNo = PubUtils.i2bLt(Integer.valueOf(PubUtils.getFromConfigs("batchNO")), 4);


            // 密文mac
            byte[] CipherDataMAC = PubUtils.HS2BA(RechargeInfo.rechargeInfoMac);
            // 密文长度
            byte[] CipherDataLen = PubUtils.i2bLt(48, 2);
            // 加密密文
            byte[] MacBuf = PubUtils.HS2BA(RechargeInfo.rechargeInfoMacBuf);
            // 快充券号
//            byte[] orderNo = PubUtils.HS2BA(RechargeInfo.CACHE_ORDERNO);
            byte[] orderNo = RechargeInfo.CACHE_BYTE_ORDERNO;
            // 上一笔充值记录
            byte[] LastRecord = PubUtils.HS2BA(recharge.Trading);
            // 通讯流水号
            byte[] PosCommSeq = RechargeInfo.CACHE_POS_COMM_SEQ;



            byte[] data = new byte[129];
            System.arraycopy(sysDateTime,   0, data,  0, 7);
            System.arraycopy(oprId,         0, data,  7, 3);
            System.arraycopy(posId,         0, data, 10, 20);
            System.arraycopy(iSam,          0, data, 30, 6);
            System.arraycopy(unitId,        0, data, 36, 4);
            System.arraycopy(mchntId,       0, data, 40, 6);
            System.arraycopy(batchNo,       0, data, 46, 4);
            System.arraycopy(CipherDataMAC, 0, data, 50, 4);
            System.arraycopy(CipherDataLen, 0, data, 54, 2);
            System.arraycopy(MacBuf,        0, data, 56, 48);
            System.arraycopy(orderNo,       0, data, 104, 4);
            System.arraycopy(LastRecord,    0, data, 108, 17);
            System.arraycopy(PosCommSeq,    0, data, 125, 4);

            Logger.info(">>>>>>>>5143<<<<<<<<<<<<<");
            Logger.info(
                    "CipherDataMAC = "  + PubUtils.BA2HS(CipherDataMAC) + "\n"
                            + "CipherDataLen = "    + PubUtils.b2iLt(CipherDataLen, 2) + "\n"
                            + "MacBuf = "           + PubUtils.BA2HS(MacBuf) + "\n"
                            + "orderNo = "          + PubUtils.b2iLt(orderNo, 4) + "\n"
                            + "LastRecord = "       + PubUtils.BA2HS(LastRecord) + "\n"
                            + "PosCommSeq = "       + PubUtils.b2iLt(PosCommSeq, 4) + "\n"
            );

            // 命令处理
            result = commandWork(NetUtils.initMessage(messageType, ver, data), RechargeInfo.URL_RECHARGE_APPLY, RechargeInfo.msg2, bean);
            if(result.codeType != Result.CODESUCCESS) {
                Logger.info(">>>>FAIL 5143 : rechargeApply");
                return result;
            }
        }catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }

        return result;
    }

    /**
     * 订单充值交易确认请求/应答
     */
    public static Result rechargeApplyConfirm(KCQTradeResponse bean) {
        Result result = null;
        try{
            /** 初始化数据报文 **/
            byte[] messageType = PubUtils.HS2BA("5145");
            byte ver = 0x01;
            byte[] sysDateTime = PubUtils.HS2BA(PubUtils.generateSysTime());
            byte[] oprId = PubUtils.HS2BA(RechargeInfo.oprId);
            byte[] POSID = NetUtils.generatePOSID(RechargeInfo.posId);
            byte[] ISAM = PubUtils.HS2BA(RechargeInfo.sam);
            byte[] unitId = PubUtils.HS2BA(RechargeInfo.unitId);
            byte[] mchntId = PubUtils.HS2BA(LocalContext.mchntId);
            byte[] CipherDataMAC = PubUtils.HS2BA(RechargeInfo.CACHE_CIPHER_DATA_MAC);//密文MAC
            byte[] CipherDataLen = PubUtils.i2bLt(32, 2);//密文长度
            byte[] MacBuf = PubUtils.HS2BA(RechargeInfo.CACHE_MACBUF);//加密密文
            byte[] orderNo = RechargeInfo.CACHE_BYTE_ORDERNO;
            byte[] CSN = PubUtils.HS2BA(RechargeInfo.Data.substring(52, 60));
            byte[] AftBal = PubUtils.HS2BA(RechargeInfo.Data.substring(30, 38));
            byte[] CardCount = PubUtils.HS2BA(RechargeInfo.Data.substring(60, 64));
            byte[] TxnTAC = PubUtils.HS2BA(RechargeInfo.Data.substring(80, 88));
            byte[] CardExp = PubUtils.HS2BA(RechargeInfo.Data.substring(100, 108));
            byte[] TacType = PubUtils.HS2BA(RechargeInfo.Data.substring(108, 110));
            byte[] PsamTransNo = PubUtils.HS2BA(RechargeInfo.rechargeInfoPsamTransNo);//钱包交易序列号
            byte TxnStatus = 0x00;
            byte[] PlivateType = PubUtils.HS2BA("0020");

            byte[] data = new byte[112];

            System.arraycopy(sysDateTime,   0, data,  0, 7);
            System.arraycopy(oprId,         0, data,  7, 3);
            System.arraycopy(POSID,         0, data, 10, 20);
            System.arraycopy(ISAM,          0, data, 30, 6);
            System.arraycopy(unitId,        0, data, 36, 4);
            System.arraycopy(mchntId,       0, data, 40, 6);
            System.arraycopy(CipherDataMAC, 0, data, 46, 4);
            System.arraycopy(CipherDataLen, 0, data, 50, 2);
            System.arraycopy(MacBuf,      	0, data, 52, 32);
            System.arraycopy(orderNo,   	0, data, 84, 4);
            System.arraycopy(CSN,        	0, data, 88, 4);
            System.arraycopy(AftBal,       	0, data, 92, 4);
            System.arraycopy(CardCount,     0, data, 96, 2);
            System.arraycopy(TxnTAC,      	0, data, 98, 4);
            System.arraycopy(CardExp,   	0, data, 102, 4);
            System.arraycopy(TacType,       0, data, 106, 1);
            System.arraycopy(PsamTransNo,   0, data, 107, 2);
            data[109]= TxnStatus;
            System.arraycopy(PlivateType,   0, data, 110, 2);

            // 命令处理
            result = commandWork(NetUtils.initMessage(messageType, ver, data), RechargeInfo.URL_RECHARGE_APPLY, RechargeInfo.msg3, bean);
            if(result.codeType != Result.CODESUCCESS) {
                Logger.info(">>>>FAIL 5145 : rechargeApply");
                return result;
            }
        }catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }

        return result;
    }

    /**
     * 签退请求
     *
     * @return 签退应答
     */
    public static Result signOutApply() {
        /** 初始化报文 */
        byte[]  messageType = PubUtils.HS2BA("6001");
        byte ver = 0x02;
        byte[]  sysDateTime = PubUtils.HS2BA(PubUtils.generateSysTime());
        byte[]  oprId       = PubUtils.HS2BA(LocalContext.oprId);
        byte[]  POSID       = NetUtils.generatePOSID(LocalContext.posId);
        byte[]  ISAM        = PubUtils.HS2BA(LocalContext.CACHE_SAM);
        byte[]  unitId      = PubUtils.HS2BA(LocalContext.unitId);
        byte[]  mchntId     = PubUtils.HS2BA(LocalContext.mchntId);
        byte[]  batchNO     = PubUtils.i2bLt(Integer.valueOf(LocalContext.batchNo), 4);

        String  POS_IC_SEQ =  PubUtils.getFromConfigs("POS_IC_SEQ");//IC_SEQ值获取
        byte[]  posIcSeq    = PubUtils.HS2BA(POS_IC_SEQ == null ? "00000000" : POS_IC_SEQ); // 签退需要维护IC_SEQ

        byte[]  posAccSeq   = new byte[]{0x00, 0x00, 0x00, 0x00};
        byte[]  posCommSeq  = new byte[]{0x00, 0x00, 0x00, 0x00};

        byte[] data = new byte[62];
        System.arraycopy(sysDateTime,       0,      data,       0,      7);
        System.arraycopy(oprId,             0,      data,       7,      3);
        System.arraycopy(POSID,             0,      data,       10,     20);
        System.arraycopy(ISAM,              0,      data,       30,     6);
        System.arraycopy(unitId,            0,      data,       36,     4);
        System.arraycopy(mchntId,           0,      data,       40,     6);
        System.arraycopy(batchNO,           0,      data,       46,     4);
        System.arraycopy(posIcSeq,          0,      data,       50,     4);
        System.arraycopy(posAccSeq,         0,      data,       54,     4);
        System.arraycopy(posCommSeq,        0,      data,       58,     4);

        /** 命令处理 **/
        Result result = commandWork(NetUtils.initMessage(messageType, ver, data), LocalContext.URL_SIGN_OUT);
        if(result.codeType != Result.CODESUCCESS) {
            Logger.info(">>>>FAIL:SignOut apply");
            return result;
        }

        /** 判断应答码 **/
        byte responseCode = result.fdBytes[25];
        if(responseCode != 0x00){
            Logger.info(">>>>FAIL:SignOut apply response.ResponseCode=0x" + PubUtils.BA2HS(new byte[]{responseCode}));
            return new Result(Result.NETCODE, null, -1 , null , responseCode);
        }

        return result;
    }

    /**
     * 签到确认
     *
     * @return 签到确认结果
     */
    public static Result signConfirm() {
        /** 初始化数据报文*/
        byte[]  messageType     = PubUtils.HS2BA("5000");
        byte    ver             = 0x02;
        byte[]  ISAM            = PubUtils.HS2BA(LocalContext.CACHE_SAM);
        byte    responseCode    = 0x00;

        byte[] data = new byte[7];
        System.arraycopy(ISAM, 0, data, 0, 6);
        data[6] = responseCode;

        /** 命令处理 **/
        Result result = commandWork(NetUtils.initMessage(messageType, ver, data), LocalContext.URL_VERIFY);

        if(result.codeType != Result.CODESUCCESS && result.codeType != Result.VSCODE)
            Logger.info(">>>>FAIL:Sign Confirm");
        else
            result = new Result(Result.CODESUCCESS, result.fdBytes, -1, null, -1);

        return result;
    }

    /**
     * 参数查询
     *
     * @param chkMode 查询模式.
     *                0x00 后台返回终端所配置的参数,终端首次使用或者更换时采用
     *                0x01 后台返回终端所需要更新的参数
     * @return 查询结果封装
     */
    public static Result paramsQuery(int chkMode) {
        if(chkMode != 0 && chkMode != 1) chkMode = 0;//chkMode的必须是0或者1,否者强制设置为0
        Logger.info(">>>> ParamsQuery: ChkMode=0x" + (chkMode == 0 ? "00" : "01"));

        /** 初始化报文 */
        byte[]  messageType = PubUtils.HS2BA("5033");
        byte    ver         = 0x02;
        byte[]  sysDateTime = PubUtils.HS2BA(PubUtils.generateSysTime());
        byte[]  oprId       = PubUtils.HS2BA(LocalContext.oprId);
        byte[]  POSID       = NetUtils.generatePOSID(LocalContext.posId);
        byte[]  ISAM        = PubUtils.HS2BA(LocalContext.CACHE_SAM);
        byte[]  unitId      = PubUtils.HS2BA(LocalContext.unitId);
        byte[]  mchntId     = PubUtils.HS2BA(LocalContext.mchntId);
        byte[]  reserved    = new byte[] {0x00, 0x00, 0x00, 0x00};

        byte[] data = new byte[51];
        System.arraycopy(sysDateTime,   0, data, 0 , 7);
        System.arraycopy(oprId,         0, data, 7 , 3);
        System.arraycopy(POSID,         0, data, 10, 20);
        System.arraycopy(ISAM,          0, data, 30, 6);
        System.arraycopy(unitId,        0, data, 36, 4);
        System.arraycopy(mchntId,       0, data, 40, 6);
        data[46] = (byte)chkMode;
        System.arraycopy(reserved,      0, data, 47, 4);

        /** 命令处理工作 */
        Result result = commandWork(NetUtils.initMessage(messageType, ver, data), LocalContext.URL_QUERY);
        if(result.codeType != Result.CODESUCCESS) {
            Logger.info(">>>> FAIL: ParamQuery");
            return result;
        }

        /** 判断应答码 **/
        byte responseCode = result.fdBytes[44];
        if(responseCode != 0x00){
            Logger.info(">>>>FAIL: ParamsQuery response.ResponseCode=0x" + PubUtils.BA2HS(new byte[]{responseCode}));
            return new Result(Result.NETCODE, null, -1 , null , responseCode);
        }

        return result;
    }

    /**
     * 参数下载请求
     *
     * @param paramCode 参数编码
     * @return 参数下载请求结果
     */
    public static Result paramDownloadApply(byte[] paramCode) {
        if(paramCode == null) throw new IllegalArgumentException("paramCode is null.");
        if(paramCode.length != 4) throw new IllegalArgumentException("paramCode.length != 2.");

        ByteArrayOutputStream   allData     = new ByteArrayOutputStream();  // 中间报文所有的反馈数据
        Result                  result;                                     // 结果反馈

        try {
            /******* 参数下载请求、应答 *******/
            byte[]  messageType = PubUtils.HS2BA("5035");
            byte    ver         = 0x01;
            byte[]  sysDateTime = PubUtils.HS2BA(PubUtils.generateSysTime());
            byte[]  POSID       = NetUtils.generatePOSID(LocalContext.posId);
            byte[]  ISAM        = PubUtils.HS2BA(LocalContext.CACHE_SAM);

            byte[] data = new byte[37];
            System.arraycopy(sysDateTime,   0, data, 0 , 7);
            System.arraycopy(POSID,         0, data, 7, 20);
            System.arraycopy(ISAM,          0, data, 27, 6);
            System.arraycopy(paramCode,     0, data, 33, 4);

            Result paramResult = commandWork(NetUtils.initMessage(messageType, ver, data), LocalContext.URL_PARAM);
            if(paramResult.codeType != Result.CODESUCCESS) {
                Logger.info(">>>>FAIL:ParamsDownload fail");

                return paramResult;
            }

            byte responseCode = paramResult.fdBytes[45];
            if(responseCode != 0x00){
                Logger.info(">>>>FAIL: ParamsDownload response.ResponseCode=0x" + PubUtils.BA2HS(new byte[]{responseCode}));
                return new Result(Result.NETCODE, null, -1 , null , responseCode);
            }

            PubWorker.analyis5036(paramResult.fdBytes); // 解析反馈数据

            /******* 参数数据内容中间报文传输 *******/
            byte[]  recordNum = new byte[] { paramResult.fdBytes[46], paramResult.fdBytes[47], paramResult.fdBytes[48], paramResult.fdBytes[49]};
            int     recordInt = PubUtils.b2iLt(recordNum, 4);// 记录总笔数

            messageType = PubUtils.HS2BA("1000");
            ver         = 0x01;
            data        = new byte[] { responseCode };

            int downTimes = 1;
            while(downTimes <= recordInt) {
                Result paramMidResult = commandWork(NetUtils.initMessage(messageType, ver, data), LocalContext.URL_PARAM_DOWNLOAD);
                if(paramMidResult.codeType != Result.CODESUCCESS) {
                    Logger.info(">>>>FAIL:Recevice data fail");
                    result = paramResult;
                    break; // 跳出for循环,返回result
                }

                /*数据解析 */
                allData.write(extractDataBlock(paramMidResult.fdBytes));

                /*最后包判断*/
                if(paramMidResult.fdBytes[8] == 1) break; //最后包标记:0-中间包,1-最后包

                downTimes ++;
            }
            // 下载次数应该不大于"记录总笔数"
            if(downTimes > recordInt) Logger.warn(">>>>WARN:Download too many times.[recordInt=" + recordInt + ", downTimes=" + downTimes + "]");

            /** 返回所有的中间数据内容 */
            result = new Result(Result.CODESUCCESS, allData.toByteArray(), -1, null, -1);
        } catch(Exception e) {
            Logger.error(">>>>FAIL:ParamsDownload fail", e);
            result = new Result(Result.NETCODE, null, -1, null, -1);
        } finally {
            cancelDownLoad(NetUtils.initMessage(PubUtils.HS2BA("1000"), (byte) 0x01, new byte[]{0x00}), LocalContext.URL_PARAM_OVER); // 结束服务器的参数下载长连接
        }

        return result;
    }

    /**
     * 交易数据上送
     *
     * @return 上送反馈结果
     */
    public static Result upload(List<String> transData) {
        Result result; // 结果反馈
        try {
            /******* 交易数据上送请求、应答 *******/
            byte[]  messageType = PubUtils.HS2BA("5037");
            byte    ver         = 0x01;
            byte[]  sysDateTime = PubUtils.HS2BA(PubUtils.generateSysTime());
            byte[]  messageID   = NetUtils.calcDataPacketName().getBytes();
            byte[]  POSID       = NetUtils.generatePOSID(LocalContext.posId);
            byte[]  unitId      = PubUtils.HS2BA(LocalContext.unitId);
            byte[]  mchntId     = PubUtils.HS2BA(LocalContext.mchntId);
            byte[]  BatchNO     = PubUtils.i2bLt(Integer.parseInt(PubUtils.getFromConfigs("batchNO")), 4);
            byte[]  ISAM        = PubUtils.HS2BA(LocalContext.CACHE_SAM);
            byte[]  messageLen  = PubUtils.i2bLt(48 + 109 * transData.size(), 4);

            byte[] data = new byte[85];
            System.arraycopy(sysDateTime,   0,     data,   0,      7);
            System.arraycopy(messageID,     0,     data,   7,      34);
            System.arraycopy(POSID,         0,     data,   41,      20);
            System.arraycopy(unitId,        0,     data,   61,      4);
            System.arraycopy(mchntId,       0,     data,   65,      6);
            System.arraycopy(BatchNO,       0,     data,   71,      4);
            System.arraycopy(ISAM,          0,     data,   75,      6);
            System.arraycopy(messageLen,    0,     data,   81,      4);

            Result uploadResult = commandWork(NetUtils.initMessage(messageType, ver, data), LocalContext.URL_DATA);
            if(uploadResult.codeType != Result.CODESUCCESS) {
                Logger.info(">>>>FAIL:Upload request fail.");
                return uploadResult;
            }

            byte responseCode = uploadResult.fdBytes[79];
            if(responseCode != 0x00) {
                Logger.info(">>>>FAIL:Upload request response fail.ResponseCode=0x" + PubUtils.BA2HS(new byte[]{responseCode}));
                return new Result(Result.NETCODE, null, -1 , null , responseCode);
            }

            PubWorker.analyis5038(uploadResult.fdBytes);

            /** 交易数据中间内容上传,为了不传重复重复数据,只传最后包*/
            data        = new byte[1 + 48 + 109 * transData.size()];
            data[0]     = 0x01;

            messageType = PubUtils.HS2BA("1100");
            ver         = 0x01;
            /* 包头数据 */
            byte[] packetHead = new byte[48];

            int     packetVer           = 0x06;
            byte[]  packetNO            = PubUtils.HS2BA(NetUtils.generatePacketNO());
            byte[]  recordsStartIndex   = PubUtils.i2bLt(48, 2);
            byte[]  singleLen           = PubUtils.i2bLt(109, 2);
            byte[]  recordsCounts       = PubUtils.i2bLt(transData.size(), 2);
            byte[]  sendUnitCode        = PubUtils.HS2BA(PubUtils.getFromConfigs("sendUnitCode"));
            byte[]  receUnitCode        = PubUtils.HS2BA(PubUtils.getFromConfigs("receUnitCode"));
            int     testFlag            = 0x00; // 0x00-正式数据 0x01-测试数据
            byte[]  individType         = new byte[] {0x02, 0x20}; // 添加个性化信息,上送商户代码

            packetHead[0] = (byte)packetVer;                            // 包格式版本号
            System.arraycopy(packetNO,          0, packetHead, 1, 16);  // 包编号
            System.arraycopy(recordsStartIndex, 0, packetHead, 17, 2);  // 记录开始位置
            System.arraycopy(singleLen,         0, packetHead, 19, 2);  // 单条记录长度
            System.arraycopy(recordsCounts,     0, packetHead, 21, 2);  // 记录总数
            System.arraycopy(sendUnitCode,      0, packetHead, 23, 4);  // 发送方业主代码
            System.arraycopy(receUnitCode,      0, packetHead, 27, 4);  // 接收方业主代码
            packetHead[31] = (byte) testFlag;                           // 测试标志
            System.arraycopy(individType,       0, packetHead, 32, 2);  // 个性化业务类型

            ///// 包头数据存储到data
            System.arraycopy(packetHead, 0, data, 1, 48);

            /* 包体数据 */
            byte[] packetBody = new byte[109];
            for(int i = 0, len = transData.size(); i < len; i++) {
                /* 添加交易记录 */
                byte[] transBytes = PubUtils.HS2BA(transData.get(i));
                System.arraycopy(transBytes, 0, packetBody, 0, 50);            // 交易类型 ~ 物理卡类型
                System.arraycopy(PubUtils.i2bLt(i + 1, 4), 0, packetBody, 50, 4);  // (包中)记录序号[1-10]
                packetBody[54] = 0x00;                                         // 消费是,填写0x00
                System.arraycopy(transBytes, 50, packetBody, 55, 7);           // 密钥及算法标识 ~ 终端交易序号

                /* 添加个性化信息 */
                byte[] personalizedInfo = new byte[47];
                byte[] perAmount   = { transBytes[7], transBytes[8], transBytes[9], transBytes[10] };
                byte[] perMchntId  = PubUtils.HS2BA(LocalContext.mchntId);
                byte[] perPOSID    = NetUtils.generatePOSID(LocalContext.posId);
                byte[] perBatchNo  = PubUtils.i2bLt(Integer.valueOf(PubUtils.getFromConfigs("batchNO")), 4);
                byte[] perOprId    = PubUtils.HS2BA(LocalContext.oprId);
                byte[] others      = {
                        (byte)0xFF,(byte)0xFF,(byte)0xFF,
                        (byte)0xFF,(byte)0xFF,(byte)0xFF,
                        (byte)0xFF,(byte)0xFF,(byte)0xFF,
                        (byte)0xFF
                };
                System.arraycopy(perAmount,  0, personalizedInfo, 0 , 4);  // 应收金额(即交易金额)
                System.arraycopy(perMchntId, 0, personalizedInfo, 4 , 6);  // 商户代码
                System.arraycopy(perPOSID,   0, personalizedInfo, 10, 20); // 设备号
                System.arraycopy(perBatchNo, 0, personalizedInfo, 30, 4);  // 批次号
                System.arraycopy(perOprId,   0, personalizedInfo, 34, 3);  // 操作员号
                System.arraycopy(others,     0, personalizedInfo, 37, 10); // 保留

                System.arraycopy(personalizedInfo, 0, packetBody, 62, 47); // 保存包体

                // 记录体存储到data
                System.arraycopy(packetBody, 0, data, 1 + 48 + i * 109, 109);
            }

            Result uploadMidResult = commandWork(NetUtils.initMessage(messageType, ver, data), LocalContext.URL_DATA_UPLOAD);
            if(uploadMidResult.fdBytes[8] != 0x00) {
                Logger.warn(">>>>FAIL:Upload data fail");
                return new Result(Result.NETCODE, null, -1 , null , responseCode);
            }
            result = uploadMidResult;
        } catch(Exception e) {
            Logger.error(">>>>FAIL:Upload data fail." +e.getMessage());
            result = new Result(Result.NETCODE, null, -1, null, -1);
        } finally {
            cancelDownLoad(new byte[] {0x00}, LocalContext.URL_DATA_OVER);
        }

        return result;
    }


    /**
     * 从中间数据报文里面提取数据块的内容
     *
     * @param fdBytes 中间数据报文
     * @return 数据块
     */
    private static byte[] extractDataBlock(byte[] fdBytes) {
        byte[] dataBlock = new byte[fdBytes.length - 13];
        System.arraycopy(fdBytes, 9, dataBlock, 0, dataBlock.length);

        return dataBlock;
    }

    /**
     * 消息处理逻辑,包括协议包体生成、发送、接收、校验反馈协议包等
     *
     * @param message 消息包体
     * @param url VCard Server地址
     * @return 反馈结果.如果反馈成功,则包含完整的消息报文
     */
    private static Result commandWork(byte[] message, String url) {
        Result receive;

        try {
            /**  向前置机发送报文 **/
            receive = NetUtils.sendMessage(message, url);
            if(receive.codeType != Result.CODESUCCESS) {
                Logger.info(">>>>FAIL:Send message to Pre-machine fail");
                return receive;
            }

            /** VCard Server 反馈data="",则说明前置机不返回数据 */
            if(receive.fdBytes.length == 0) {
                Logger.info(">>>>WARN: VCardServer message is BLANK");
                return new Result(Result.VSCODE, null, -1, null, -1);
            }

            /** 校验反馈的包体格式 */
            int check = NetUtils.checkCommand(receive.fdBytes);
            if(check != 0) {
                Logger.info(">>>>FAIL:Check Pre-machine message's CRC fail.");
                return new Result(Result.BYTESCODE, null, check, null, -1);
            }
        } catch(Exception e) {
            Logger.error(">>>>FAIL: command work error.", e);
            receive = new Result(Result.VSCODE, null, -1, null, -1);
        }

        return receive;
    }

    /**
     * 消息处理逻辑,包括协议包体生成、发送、接收、校验反馈协议包等
     *
     * @param message 消息包体
     * @param url VCard Server地址
     * @return 反馈结果.如果反馈成功,则包含完整的消息报文
     */
    private static Result commandWork(byte[] message, String url, String msgType, KCQTradeResponse bean) {
        Result receive;

        try {
            /**  向前置机发送报文 **/
            receive = NetUtils.sendMessage(message, url, msgType, bean);
            if(receive.codeType != Result.CODESUCCESS) {
                Logger.info(">>>>FAIL:Send message to Pre-machine fail");
                return receive;
            }

            /** VCard Server 反馈data="",则说明前置机不返回数据 */
            if(receive.fdBytes.length == 0) {
                Logger.info(">>>>WARN: VCardServer message is BLANK");
                return new Result(Result.VSCODE, null, -1, null, -1);
            }

            /** 校验反馈的包体格式 */
            int check = NetUtils.checkCommand(receive.fdBytes);
            if(check != 0) {
                Logger.info(">>>>FAIL:Check Pre-machine message's CRC fail.");
                return new Result(Result.BYTESCODE, null, check, null, -1);
            }
        } catch(Exception e) {
            Logger.error(">>>>FAIL: command work error.", e);
            receive = new Result(Result.VSCODE, null, -1, null, -1);
        }

        return receive;
    }

    /**
     * 取消参数下载
     *
     * @param code 错误码
     */
    private static void cancelDownLoad(byte[] code, String url) {
        Result downOver = commandWork(code, url);
        if(downOver.codeType == Result.VSCODE)
            Logger.info(">>>> SUCCESS:Cancel VCardServer socket.H-ASCII: " + PubUtils.BA2HS(code) + ", Result.codeType=" + downOver.codeType);
        else
            Logger.warn(">>>> FAIL:Cancel VCardServer socket.H-ASCII: " + PubUtils.BA2HS(code) + ", Result.codeType=" + downOver.codeType);
    }

}

