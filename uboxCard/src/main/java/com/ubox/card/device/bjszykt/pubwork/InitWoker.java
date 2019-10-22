package com.ubox.card.device.bjszykt.pubwork;

import com.ubox.card.device.bjszykt.localwork.LocWorker;
import com.ubox.card.device.bjszykt.network.NetWorker;
import com.ubox.card.device.bjszykt.network.paramdownload.*;
import com.ubox.card.util.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InitWoker {

    /**
     * 参数文件读取到内存
     */
    public static void paramsContextInit() {
        cacheBlackList();   // 黑名单缓存
        cacheConsume();     // 消费可用卡参数格式
        updateBlacklist();  // 增量黑名单处理
        sorCardTypelist();  // 充值卡类型参数
        cardAttrlist();  	// 卡片属性定义参数格式
        greylist();			//灰名单
    }

    /**
     * 增量黑名单处理
     */
    private static void updateBlacklist() {
        /* 读取增量黑名单 */
        String ublPath = LocalContext.workPath + File.separator + LocalContext.UPDATA_BLACK_LIST;
        List<String> ubll = PubUtils.readFileByLine(ublPath);

        if(ubll.size() == 0) {
            Logger.info(">>>>No update blacklist");
            return;
        }

        StringBuilder sb = new StringBuilder(ubll.size() * 18);
        for(String s : ubll)  sb.append(LocalContext.LINE_SEPARATOR).append(s);
        Logger.info(">>>>Update blacklist:\n" + sb.toString());

        /* 修改本地黑名单缓存 */
        for(String us : ubll) {
            String flag     = us.substring(us.length() - 2, us.length());
            String cardNo   = us.substring(0, us.length() - 2);

            if(flag.equals("01"))
                LocalContext.CACHE_BLACK_LIST.put(Long.parseLong(cardNo), null);
            else if(flag.equals("02"))
                LocalContext.CACHE_BLACK_LIST.remove(Long.parseLong(cardNo));
            else {
                Logger.warn(">>>>WARN:Unknow update blacklist flag, flag=" + us);
                return;
            }
        }

        /* 修改增量黑名单文件 和 黑名单文件 */
        File ublFile = PubUtils.fileGetOrCreate(ublPath, "update blacklist");
        PubUtils.writeData2File(ublFile, false, "", 1);

        String blacklistPath = LocalContext.workPath + File.separator + LocalContext.BLACK_LIST;
        File   blacklistFile = PubUtils.fileGetOrCreate(blacklistPath, "blacklist");

        StringBuilder blackSb = new StringBuilder(LocalContext.CACHE_BLACK_LIST.size() * 16);
        for(Long lb : LocalContext.CACHE_BLACK_LIST.keySet())  blackSb.append(String.valueOf(lb));
        PubUtils.writeData2File(blacklistFile, false, blackSb.toString(), 16);
    }

    /**
     * 黑名单数据缓存到内存
     */
    private static void cacheBlackList() {
        try {
            String blackListFilePath = LocalContext.workPath + File.separator + LocalContext.BLACK_LIST;
            List<String> bll = PubUtils.readFileByLine(blackListFilePath);
            for(String cn : bll)  LocalContext.CACHE_BLACK_LIST.put(Long.parseLong(cn), null);
        } catch(Exception e) {
            Logger.error(">>>>ERROR:Cache blacklist fail", e);
        }
    }

    /**
     * 消费可用卡数据到内存
     */
    private static void cacheConsume() {
        try {
            String consumeCardType = LocalContext.workPath + File.separator + LocalContext.CONSUM_CARD_TYPE;
            for(String s : PubUtils.readFileByLine(consumeCardType)){
            	LocalContext.CACHE_CONSUME.add(s.substring(0, 4));
            }
        } catch(Exception e) {
            Logger.error(">>>>ERROR:Cache consume card type fail",e);
        }
    }
    
    /**
     * 充值卡类型参数到内存
     */
    private static void sorCardTypelist() {
        try {
            String consumeCardType = LocalContext.workPath + File.separator + LocalContext.STORED_CARD_TYPE;
            for(String s : PubUtils.readFileByLine(consumeCardType)){
            	LocalContext.SOR_CARD_TYPE.add(s);
            }
        } catch(Exception e) {
            Logger.error(">>>>ERROR:Sor card type fail",e);
        }
    }
    
    /**
     * 灰名单到内存
     */
    private static void greylist() {
        try {
            String consumeCardType = LocalContext.workPath + File.separator + LocalContext.GREY_LIST;
            for(String s : PubUtils.readFileByLine(consumeCardType)){
            	LocalContext.CARD_GREY_LIST.add(s);
            }
        } catch(Exception e) {
            Logger.error(">>>>ERROR:Sor card type fail",e);
        }
    }
    
    /**
     * 卡片属性定义参数到内存
     */
    private static void cardAttrlist() {
        try {
            String consumeCardType = LocalContext.workPath + File.separator + LocalContext.CARD_ATTR;
            for(String s : PubUtils.readFileByLine(consumeCardType)){
            	LocalContext.CARD_ATTR_LIST.add(s);
            }
        } catch(Exception e) {
            Logger.error(">>>>ERROR:Card attr list fail",e);
        }
    }

    /**
     * 本地参数初始化
     *
     * @return 0-初始化成功;非0-初始化失败
     */
    public static int localContextInit() {
        try {
            /* 签到指令 */
            Result result = LocWorker.readVersion();

            if(result.codeType != Result.CODESUCCESS) {
            	return result.codeType;
            }

            PubWorker.parseVersion(result.fdBytes);
        } catch(Exception e) {
            Logger.error(">>>> FAIL:Local sign in", e);
            return -1;
        }

        return 0;
    }

    /**
     * 批上送 + 签退,防止数据漏传
     *
     * @return 0-成功;非0-失败
     */
    public static int uploadAndSignOut() {
        try {
            /* 上传本地销售数据 */
            int upcode = LocWorker.uploadTransData();
            if(upcode != 0) {
                Logger.warn(">>>>FAIL:Upload data fail");
                return upcode;
            }

            /* 签退申请 */
            Result result = NetWorker.signOutApply();
            if(result.codeType != Result.CODESUCCESS) return result.codeType;
            PubWorker.parseSignOutReplay(result.fdBytes);

            /* 签退复位 */
            result = LocWorker.signOutReset();
            if(result.codeType != Result.CODESUCCESS) return result.codeType;
        }catch(Exception e) {
            Logger.error(">>>>FAIL:batch upload data AND sign out apply fail", e);
            return -1;
        }

        return 0;
    }

    /**
     * 参数查询
     *
     * @param chkmode 查询模式
     * @return 参数查询更新标志-ParamFlag.查询失败返回-1
     */
    public static int paramsQuery(int chkmode) {
        Result qr = NetWorker.paramsQuery(chkmode);

        if(qr.codeType != Result.CODESUCCESS) {
            Logger.warn(">>>>FAIL: ParamsQuery fail");
            return -1;
        } else {
            int paramFlag =  (qr.fdBytes[45] << 24) | (qr.fdBytes[46] << 16) | (qr.fdBytes[47] << 8) | qr.fdBytes[48];
            Logger.info(">>>>ParamsFlag:" + PubUtils.BA2HS(PubUtils.i2bLg(paramFlag, 4)));
            return paramFlag;
        }
    }

    /**
     * 检查本地参数是否完整
     *
     * @return 0-成功;非0-失败
     */
    public static int checkLocalContext() {
        String workpath  = LocalContext.workPath;
        String separator = File.separator;

        int rc = 0;

        if(checkFile(workpath + separator + LocalContext.BLACK_LIST) != 0) { // 黑名单
            rc ++;
        }
        if(checkFile(workpath + separator + LocalContext.CONSUM_CARD_TYPE) != 0) { // 消费卡类型参数
            rc ++;
        }
        if(checkFile(workpath + separator + LocalContext.OPERATION_PARAM) != 0) { // 终端运营参数
            rc ++;
        }
        if(checkFile(workpath + separator + LocalContext.STORED_CARD_TYPE) != 0) { // 充值卡类型参数
            rc ++;
        }
        if(checkFile(workpath + separator + LocalContext.CARD_ATTR) != 0) { // 卡片属性参数
            rc ++;
        }
        if(checkFile(workpath + separator + LocalContext.GREY_LIST) != 0) { // 灰名单
            rc ++;
        }

        return rc;
    }

    /**
     * 检查指定的文件是否存在且size不为0
     *
     * @param filePath 文件路径
     * @return 0-符合要求;非0-不符合要求
     */
    private static int checkFile(String filePath) {
        File file = new File(filePath);
        if(!file.exists()) {
            Logger.warn(">>>>WARN: filePath is not exists, FilePath=" + filePath);
            return 1;
        }
        if(!file.isFile()) {
            Logger.warn(">>>>WARN: filePath is not a file, FilePath=" + filePath);
            return 2;
        }
        if(file.length() == 0){
            Logger.warn(">>>>WARN: file.length = 0, FilePath=" + filePath);
            return 3;
        }

        return 0;
    }

    /**
     * 设备签到
     *
     * @return 0-签到成功;非0-签到失败
     */
    public static int signInWork() {
        int rcode;
        try {
            Result result = signIn();
            if(result.codeType == Result.CODESUCCESS)
                return 0;// 签到成功,返回
            rcode = result.codeType;

            /** 签到失败处理*/
            if(result.codeType == Result.BYTESCODE) {
                Logger.info(">>>>FAIL: byte stream, bytesCode=" + result.bytesCode);
            } else if(result.codeType == Result.HDCODE) {
                Logger.info(">>>>FAIL: device fail, hdCode=" + result.hdCode);
            } else if(result.codeType == Result.NETCODE) {
                String netCode = PubUtils.BA2HS(new byte[]{(byte) result.netCode});
                Logger.info(">>>>FAIL: Pre-machine fail, netCode=0x" + netCode);

                if(result.netCode == 0x0F)
                    signInParamDownload(); // 需要进行参数下载
                else if(result.netCode == 0x28)
                    updateICSEQ(result.fdBytes); // 更新流水号
                else if(result.netCode == 0x29)
                    updateICSEQ(result.fdBytes);
                else if(result.netCode == 0x2A)
                    uploadAndSignOut(); // 0x2A(终端已签到),进行签退
                else Logger.warn(">>>>WARN: Do not process netCode. netCode=0x" + netCode);
            } else if(result.codeType == Result.VSCODE){
                Logger.info(">>>>WARN: Do not process VCardServer blank message");
            } else {
                Logger.warn(">>>>WARN: Unknow WorkResult type, codeType=" + result.codeType);
            }
        } catch(Exception e) {
            Logger.error(">>>>FAIL: sign error", e);
            return -1;
        }

        return rcode;
    }

    /**
     * 更新流水号
     * @param fdBytes 前置机返回数据
     */
    private static void updateICSEQ(byte[] fdBytes) {
        byte[] posIcSeq     = new byte[4];
        System.arraycopy(fdBytes, 42, posIcSeq, 0, 4);// 终端IC交易流水号

        try {
            HashMap<String, String> kvs = new HashMap<String, String>();
            kvs.put("POS_IC_SEQ", PubUtils.BA2HS(posIcSeq));

            if(PubUtils.configsPerisit(kvs) == 0) {
                Logger.info(">>>>SUCCESS: Persist POS_IC_SEQ successfully.Pre-machine's PosIcSeq=" + PubUtils.BA2HS(posIcSeq));
            }
        } catch(Exception e) {
            Logger.error(">>>>FAIL:Persist POS_IC_SEQ", e);
        }
    }

    private static Result signIn() {
        Result result;
        try {
            /* 签到指令 */
            result = LocWorker.sign();
            if(result.codeType != Result.CODESUCCESS) return result;
            PubWorker.parseSignReply(result.fdBytes);

            /* 签到申请 */
            result = NetWorker.signApply();
            if(result.codeType != Result.CODESUCCESS)  return result;
            PubWorker.parseSignApplyReplay(result.fdBytes);

            /* 签到认证指令 */
            result = LocWorker.signCert();
            if(result.codeType != Result.CODESUCCESS) return result;
            /* 签到确认 */
            result = NetWorker.signConfirm();

            if(result.codeType != Result.CODESUCCESS) return result;
        } catch(Exception e) {
            Logger.error(">>>>FAIL:Sign In work error", e);
            result = new Result(Result.BYTESCODE, null, 55, null, -1);
        }

        return result;
    }

    /**
     * 签到失败,进行参数下载
     */
    private static void signInParamDownload() {
        int paramFlag = paramsQuery(1);
        if(paramFlag == -1) return;// 参数查询失败

        if((paramFlag & 0x80000000) != 0) new BlackListWorker().downloadWork();          // 黑名单
        if((paramFlag & 0x40000000) != 0) new SorCardTypeWorker().downloadWork();        // 储值卡类型
        if((paramFlag & 0x20000000) != 0) new CmCardTypeWorker().downloadWork();         // 消费卡类型
        if((paramFlag & 0x10000000) != 0) new ReCardWorker().downloadWork();             // 退卡
        if((paramFlag & 0x08000000) != 0) new CommWorker().downloadWork();               // 通讯参数
        if((paramFlag & 0x04000000) != 0) new OperationWorker().downloadWork();          // 终端运营
        if((paramFlag & 0x02000000) != 0) new AutoRunWorker().downloadWork();            // 自动终端运行
        if((paramFlag & 0x01000000) != 0) new CardAttrWorker().downloadWork();           // 卡片属性参数
        if((paramFlag & 0x00800000) != 0) new RegBlackListWorker().downloadWork();       // 区域黑名单
        if((paramFlag & 0x00400000) != 0) new TerServiceWorker().downloadWork();         // 终端业务功能
        if((paramFlag & 0x00200000) != 0) new TotalNumCardWorker().downloadWork();       // 计次卡
        if((paramFlag & 0x00100000) != 0) new UpdataBlacklistWorker().downloadWork();    // 增量黑名单参数
        if((paramFlag & 0x00010000) != 0) new GreyListWorker().downloadWork();   		 // 灰名单
    }

    public static List<DownloadWorker> parseDownLoadparams(int paramFlag) {
        List<DownloadWorker> downList = new ArrayList<DownloadWorker>();

        if((paramFlag & 0x80000000) != 0) downList.add(new BlackListWorker());          // 黑名单
        if((paramFlag & 0x40000000) != 0) downList.add(new SorCardTypeWorker());        // 储值卡类型
        if((paramFlag & 0x20000000) != 0) downList.add(new CmCardTypeWorker());         // 消费卡类型
        if((paramFlag & 0x10000000) != 0) downList.add(new ReCardWorker());             // 退卡
        if((paramFlag & 0x08000000) != 0) downList.add(new CommWorker());               // 通讯参数
        if((paramFlag & 0x04000000) != 0) downList.add(new OperationWorker());          // 终端运营
        if((paramFlag & 0x02000000) != 0) downList.add(new AutoRunWorker());            // 自动终端运行
        if((paramFlag & 0x01000000) != 0) downList.add(new CardAttrWorker());           // 卡片属性参数
        if((paramFlag & 0x00800000) != 0) downList.add(new RegBlackListWorker());       // 区域黑名单
        if((paramFlag & 0x00400000) != 0) downList.add(new TerServiceWorker());         // 终端业务功能
        if((paramFlag & 0x00200000) != 0) downList.add(new TotalNumCardWorker());       // 计次卡
        if((paramFlag & 0x00100000) != 0) downList.add(new UpdataBlacklistWorker());    // 增量黑名单参数
        if((paramFlag & 0x00010000) != 0) downList.add(new GreyListWorker());    		// 灰名单

        return downList;
    }
}
