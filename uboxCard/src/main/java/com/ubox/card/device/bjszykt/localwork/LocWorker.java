package com.ubox.card.device.bjszykt.localwork;

import static com.ubox.card.device.bjszykt.pubwork.PubUtils.BA2HS;
import static com.ubox.card.device.bjszykt.pubwork.PubUtils.HS2BA;
import static com.ubox.card.device.bjszykt.pubwork.PubUtils.b2iLt;
import static com.ubox.card.device.bjszykt.pubwork.PubUtils.i2bLt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ubox.card.core.serial.RS232Worker;
import com.ubox.card.device.bjszykt.network.NetWorker;
import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.device.bjszykt.pubwork.Result;
import com.ubox.card.device.bjszykt.rs232.RS232Walker;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

public class LocWorker {
    private static int IO_TIMEOUT;
    private static RS232Walker rs232;

    static {
        IO_TIMEOUT = 11000;
        rs232 = new RS232Walker( null, 115200, RS232Worker.DATABITS_8, RS232Worker.STOPBITS_1, RS232Worker.PARITY_NONE );
    }

    // ======================= return code ===========================
    public static final int SUCCESS         = 0;  // 操作成功
    public static final int FAIL            = 4;  // 操作失败
    public static final int FIND_TIMEOUT    = 1;  // 寻卡超时
    public static final int IN_BLACK_LIST   = 2;  // 黑名单卡
    public static final int NO_CONSUME_CARD = 3;  // 不可消费卡
    public static final int IN_GREY_LIST 	= 5;  // 灰名单卡

    /**
     * 寻卡结果
     *
     * @return 0-寻卡成功,非0-寻卡失败
     */
    public static int findCard(String serNo) {
        long start = System.currentTimeMillis();
        long findTime = 20000L;
        while(true) {
            //中断寻卡处理
            if(Utils.isCancel(serNo)) 
            	return 415; // 取消刷卡
            if(System.currentTimeMillis() - start > findTime) 
            	return FIND_TIMEOUT; // 寻卡超时,返回
            
            int codeType = commandWork("AA00").codeType;
            if(codeType == 0){
            	return SUCCESS;// 寻卡成功,返回
            } else if(codeType == Result.HARDWARE){ 
            	return FAIL; 
            }
        }
    }

    /**
     * 返回读卡应答报文的数据域
     *
     * @return 数据域,[0]=应答代码,其他见协议
     */
	public static String[] readCard() {
		String[] readInfo = new String[11];

		Result read = commandWork("AA010600");
		if (read.codeType != 0) {
			readInfo[0] = read.hdCode;
			return readInfo;
		}// 读卡失败

		analyzeReadCard(read.fdBytes, readInfo);

		return readInfo;
    }

    /**
     * 返回读卡应答报文的数据域
     *
     * @return 数据域,[0]=应答代码,其他见协议
     */
    public static String[] readCard1() {
        String[] readInfo = new String[12];
        readInfo[0] = "CUOWU";

		Result read = commandWork("AA010200");
		if (read.codeType != 0) {
			readInfo[0] = read.hdCode;
			return readInfo;
		}// 读卡失败

        analyzeReadCard1(read.fdBytes, readInfo);

        return readInfo;
    }

    /**
     * 解析读卡应答报文
     *
     * @param fdBytes 读卡返回数据
     * @param readInfo 存储解析的数据域
     */
    private static void analyzeReadCard(byte[] fdBytes, String[] readInfo) {
        try {
            readInfo[0]  = new String(fdBytes,   5,    4);  // 应答代码
            readInfo[1]  = new String(fdBytes,   9,    8);  // CSN
            readInfo[2]  = new String(fdBytes,   17,  16);  // 用户卡号
            readInfo[3]  = new String(fdBytes,   33,   2);  // 卡类型
            readInfo[4]  = new String(fdBytes,   35,   2);  // 物理卡类型
            readInfo[5]  = new String(fdBytes,   37,   2);  // 卡状态
            readInfo[6]  = new String(fdBytes,   39,   8);  // 发行日期
            readInfo[7]  = new String(fdBytes,   47,   8);  // 卡失效日期
            readInfo[8]  = new String(fdBytes,   55,   8);  // 公共钱包余额
            readInfo[9]  = new String(fdBytes,   63,   4);  // 卡押金
            readInfo[10] = new String(fdBytes,   67,   2);  // 计次类型

            Logger.info(
                    "\n>>>> READ CARD <<<<" +
                    "\n>>>> Answer Code              :" + readInfo[0] +
                    "\n>>>> CSN                      :" + readInfo[1] +
                    "\n>>>> User Card Number         :" + readInfo[2] +
                    "\n>>>> Card Type                :" + readInfo[3] +
                    "\n>>>> Physical Card Type       :" + readInfo[4] +
                    "\n>>>> Card Status              :" + readInfo[5] +
                    "\n>>>> Card Released Date       :" + readInfo[6] +
                    "\n>>>> Card Expriation Date     :" + readInfo[7] +
                    "\n>>>> Public Wallet Balance    :" + readInfo[8] + "(HEX)=" + PubUtils.b2iLt(PubUtils.HS2BA(readInfo[8]), 4) + "(DEC)" +
                    "\n>>>> Card Deposit             :" + readInfo[9] + "(HEX)=" + PubUtils.b2iLt(PubUtils.HS2BA(readInfo[9]), 4) + "(DEC)" +
                    "\n>>>> Meter Sub-genre          :" + readInfo[10]
            );

            /** 缓存脱机消费使用字段 */
            LocalContext.CACHE_CSN           = readInfo[1];
            LocalContext.CACHE_PUB_BALANCE   = readInfo[8];

        } catch(Exception e) {
            Logger.error(">>>> Read Card Analyze FAIL", e);
            readInfo[0] = "CUOWU";
        }
    }

    /**
     * 解析读卡应答报文
     *
     * @param fdBytes 读卡返回数据
     * @param readInfo 存储解析的数据域
     */
    private static void analyzeReadCard1(byte[] fdBytes, String[] readInfo) {
        try {
            readInfo[0]  = new String(fdBytes,   5,    4);  // 应答代码
            readInfo[1]  = new String(fdBytes,   9,    8);  // CSN
            readInfo[2]  = new String(fdBytes,   17,  16);  // 用户卡号
            readInfo[3]  = new String(fdBytes,   33,   2);  // 卡类型
            readInfo[4]  = new String(fdBytes,   35,   2);  // 物理卡类型
            readInfo[5]  = new String(fdBytes,   37,   2);  // 卡状态
            readInfo[6]  = new String(fdBytes,   39,   8);  // 发行日期
            readInfo[7]  = new String(fdBytes,   47,   8);  // 卡失效日期
            readInfo[8]  = new String(fdBytes,   55,   8);  // 公共钱包余额
            readInfo[9]  = new String(fdBytes,   63,   4);  // 卡押金
            readInfo[10] = new String(fdBytes,   67,   2);  // 计次类型
            readInfo[11] = new String(fdBytes,   69,   34);  // 计次类型

            Logger.info(
                    "\n>>>> READ CARD <<<<" +
                            "\n>>>> Answer Code              :" + readInfo[0] +
                            "\n>>>> CSN                      :" + readInfo[1] +
                            "\n>>>> User Card Number         :" + readInfo[2] +
                            "\n>>>> Card Type                :" + readInfo[3] +
                            "\n>>>> Physical Card Type       :" + readInfo[4] +
                            "\n>>>> Card Status              :" + readInfo[5] +
                            "\n>>>> Card Released Date       :" + readInfo[6] +
                            "\n>>>> Card Expriation Date     :" + readInfo[7] +
                            "\n>>>> Public Wallet Balance    :" + readInfo[8] + "(HEX)=" + PubUtils.b2iLt(PubUtils.HS2BA(readInfo[8]), 4) + "(DEC)" +
                            "\n>>>> Card Deposit             :" + readInfo[9] + "(HEX)=" + PubUtils.b2iLt(PubUtils.HS2BA(readInfo[9]), 4) + "(DEC)" +
                            "\n>>>> Meter Sub-genre          :" + readInfo[10]+
                            "\n>>>> Last Date     			 :" + readInfo[11] 
            );

            /** 缓存脱机消费使用字段 */
            LocalContext.CACHE_CSN           = readInfo[1];
            LocalContext.CACHE_PUB_BALANCE   = readInfo[8];
            LocalContext.CACHE_CARD_COUNT	 = PubUtils.b2iLt(PubUtils.HS2BA(readInfo[9]), 4)+"";

        } catch(Exception e) {
            Logger.error(">>>> Read Card Analyze FAIL", e);
            readInfo[0] = "CUOWU";
        }
    }

    /**
     * 返回脱机消费应答报文的数据域
     *
     * @param money 消费金额,单位分
     * @return 数据域,[0]=应答代码,其他见协议
     */
    public static String[] offlineConsume(int money) {
        String[] consumeData = new String[20];

        /** 脱机钱包消费 */
        String POS_IC_SEQ =  PubUtils.getFromConfigs("POS_IC_SEQ");//IC_SEQ值获取
        LocalContext.CACHE_POS_IC_SEQ = POS_IC_SEQ == null ? LocalContext.CACHE_POS_IC_SEQ : POS_IC_SEQ;

        String cmd          = "AA02";
        String CSN          = LocalContext.CACHE_CSN;
        String pubBalance   = LocalContext.CACHE_PUB_BALANCE;
        String moneyHA      = PubUtils.BA2HS(PubUtils.i2bLt(money, 4));
        String SEQ          = LocalContext.CACHE_POS_IC_SEQ;
        String TIME         = PubUtils.generateSysTime();

        Result consumer = commandWork(cmd + CSN + pubBalance + moneyHA + SEQ + TIME);
        /** 判断脱机消费结果 */
        if(consumer.codeType != 0) {
            Logger.warn(">>>> FAIL: offline consume");
            consumeData[0] = consumer.hdCode;
            return consumeData;
        }

        /** 交易顺序号+1, 并持久化*/
        LocalContext.CACHE_POS_IC_SEQ = BA2HS(i2bLt(b2iLt(HS2BA(LocalContext.CACHE_POS_IC_SEQ), 4) + 1, 4));
        HashMap<String, String> pos_ic_seq_map = new HashMap<String, String>();
        pos_ic_seq_map.put("POS_IC_SEQ", LocalContext.CACHE_POS_IC_SEQ);
        PubUtils.configsPerisit(pos_ic_seq_map);

        /** 解析脱机消费反馈结果,并且持久化脱机消费数据*/
        analyzeConsume(consumer.fdBytes, consumeData);
        if(consumeData[0].equals("AA00")) {
            persitConsumeData(consumeData);//解析成功的数据才进行持久化工作
        }else {
            Logger.warn(">>>> FAIL: analyze consume result." + PubUtils.BA2HS(consumer.fdBytes));
        }

        return consumeData;
    }

    // uplaod lock,上传数据的网络并发安全锁
    private static final Object NET_LOCK = new Object();

    /**
     * 上传本地销售数据
     *
     * @return 0-上传成功;非0-上传失败
     */
    public static int uploadTransData() {
        String transDataFilePath  = LocalContext.workPath + File.separator + LocalContext.TRANSACTION_DATA;
        File transDataFile        = PubUtils.fileGetOrCreate(transDataFilePath, "交易数据文件");

        if(transDataFile == null) {
            Logger.warn(">>>> FAIL: get trans_data.dat file");
            return 1;
        }

        synchronized(NET_LOCK) {
            List<String> transDataList = PubUtils.readFileByLine(transDataFilePath);
            if(transDataList.size() == 0) {
                Logger.info(">>>> No data from trans_data.dat");
                return 0;// 正常返回
            }
            int dataLen = transDataList.get(0).length();// 单条数据长度

            List<String> copyList = new ArrayList<String>(transDataList.size());
            List<List<String>> resultList = splitsListByStep(transDataList, 10);
            for(List<String> spList : resultList) {
                int upcode = uploadData(spList);// 上传数据
                if(upcode == 0) {
                    for(String data : spList) copyList.add(data); // 复制要上传的数据
                }else {
                    Logger.warn(">>>> FAIL: upload data.");
                    return 2; // 退出,重新上传
                }
            }
            /* 删除上传成功的数据 */
            List<String> uploadFailDataList     = transDataList.subList(copyList.size(), transDataList.size());
            List<String> uploadSuccessDataList  = transDataList.subList(0, copyList.size());

            StringBuilder failSbt = new StringBuilder();
            StringBuilder failSbg = new StringBuilder();
            for(String data : uploadFailDataList)  {
                failSbt.append(data);
                failSbg.append(data).append(LocalContext.LINE_SEPARATOR);
            }
            if(uploadFailDataList.size() != 0) {
                Logger.info(">>>> WARN: part of trans_data upload.");
            }else {
                Logger.info(">>>> SUCCESS: trans_data upload.");
            }

            StringBuilder successSbg = new StringBuilder();
            for(String data : uploadSuccessDataList)  {
                successSbg.append(data).append(LocalContext.LINE_SEPARATOR);
            }
            if(uploadSuccessDataList.size() != 0) {
                Logger.info(">>>> upload data:\n" + successSbg.toString());
            }

            int wrcode = PubUtils.writeData2File(transDataFile, false, failSbt.toString(), dataLen);
            if(wrcode == 0) {
                Logger.info(">>>> SUCCESS: update trans_data.dat");
            } else {
                Logger.warn(">>>> FAIL: updata trans_data.dat");
            }
        }

        return 0;
    }

    /**
     *上传交易数据到前置机
     *
     * @param dataList 数据链表
     * @return 0-上传成功;非0-上传失败
     */
    private static int uploadData(List<String> dataList) {
        int upcode = NetWorker.upload(dataList).bytesCode;
        if(upcode != Result.CODESUCCESS) {
            Logger.warn(">>>> FAIL: data upload");
            return 1;
        }else {
            Logger.info(">>>> SUCCESS: data upload");
            return 0;
        }
    }

    /**
     * 解析脱机消费反馈结果
     *
     * @param fdBytes 脱机消费底层反馈数据
     * @param consumeData 存储解析数据域
     */
    private static void analyzeConsume(byte[] fdBytes, String[] consumeData) {
        try {
            consumeData[0]  = new String(fdBytes,   5,    4);                       // 应答代码
            consumeData[1]  = new String(new byte[]{fdBytes[9], fdBytes[10]});      // 交易类型
            consumeData[2]  = new String(fdBytes,   11,   12);                      // SAM卡号
            consumeData[3]  = new String(fdBytes,   23,   8);                       // 交易金额
            consumeData[4]  = new String(fdBytes,   31,   8);                       // 交易顺序号
            consumeData[5]  = new String(fdBytes,   39,   8);                       // 卡内余额
            consumeData[6]  = new String(fdBytes,   47,   8);                       // 交易日期
            consumeData[7]  = new String(fdBytes,   55,   6);                       // 交易时间
            consumeData[8]  = new String(fdBytes,   61,   8);                       // 卡序列号
            consumeData[9]  = new String(fdBytes,   69,   4);                       // 卡交易计数
            consumeData[10] = new String(fdBytes,   73,   4);                       // 城市代码
            consumeData[11] = new String(fdBytes,   77,   4);                       // 行业编号
            consumeData[12] = new String(fdBytes,   81,   8);                       // 卡发行号
            consumeData[13] = new String(fdBytes,   89,   8);                       // TAC
            consumeData[14] = new String(fdBytes,   97,   8);                       // 交易前余额
            consumeData[15] = new String(new byte[]{fdBytes[105], fdBytes[106]});   // 卡类型
            consumeData[16] = new String(new byte[]{fdBytes[107], fdBytes[108]});   // 卡物理类型
            consumeData[17] = new String(new byte[]{fdBytes[109], fdBytes[110]});   // 密钥及算法标识
            consumeData[18] = new String(fdBytes,   111,   4);                      // 钱包交易序号
            consumeData[19] = new String(fdBytes,   115,   8);                      // 终端交易序号

            Logger.info(
                    "\n>>>> analyze consume result <<<<" +
                    "\n>>>> Transaction Type             :" + consumeData[1] +
                    "\n>>>> sam Card Number              :" + consumeData[2] +
                    "\n>>>> Transaction Amount           :" + consumeData[3] +
                    "\n>>>> Transaction Sequence Number  :" + consumeData[4] +
                    "\n>>>> Card Balance                 :" + consumeData[5] + "(HEX)=" + b2iLt(HS2BA(consumeData[5]), 4) + "(DEC)" +
                    "\n>>>> Transaction Date             :" + consumeData[6] +
                    "\n>>>> Transaction Time             :" + consumeData[7] +
                    "\n>>>> Card Serial Number           :" + consumeData[8] +
                    "\n>>>> Transaction count            :" + consumeData[9] +
                    "\n>>>> City Code                    :" + consumeData[10] +
                    "\n>>>> Industry Number              :" + consumeData[11] +
                    "\n>>>> Card Issue Number            :" + consumeData[12] +
                    "\n>>>> TAC                          :" + consumeData[13] +
                    "\n>>>> Before Transaction Balance   :" + consumeData[14] +
                    "\n>>>> Card Type                    :" + consumeData[15] +
                    "\n>>>> Physical Card Type           :" + consumeData[16] +
                    "\n>>>> Key Or Algorithm Identifies  :" + consumeData[17] +
                    "\n>>>> Wallet Transaction No.       :" + consumeData[18] +
                    "\n>>>> Terminal Transaction NO.     :" + consumeData[19]
            );
        } catch(Exception e) {
            Logger.error(">>>> FAIL: analyze consume result", e);
            consumeData[0] = "AABB";
        }
    }

    /**
     * 持久化脱机消费数据
     *
     * @param consumeData 脱机消费数据
     */
    private static void persitConsumeData(String[] consumeData) {
        StringBuilder sb = new StringBuilder(60);
        for(int i = 1; i < 20; i++) sb.append(consumeData[i]);

        String transFilePath = LocalContext.workPath + File.separator + LocalContext.TRANSACTION_DATA;
        File transFile       = PubUtils.fileGetOrCreate(transFilePath, "trans_data.dat");

        if(transFile == null) {
            Logger.warn(">>>> FAIL: create trans_data.dat");
            LocalContext.VCARD_STATUS = LocalContext.TRAN_DATA_ERROR;
        }

        String data = sb.toString();
        int peistCode = PubUtils.writeData2File(transFile, true, data, data.length());
        if(peistCode != 0)  Logger.warn(">>>> FAIL: insert trans_data.dat");
    }

    /**
     * 锁卡操作
     * @param cardNO 卡号
     */
    public static void lockCard(String cardNO) {
        try {
            String code         = "AA03";
            String POS_IC_SEQ   = PubUtils.getFromConfigs("POS_IC_SEQ");//IC_SEQ值获取
            String TIME         = PubUtils.generateSysTime();

            Logger.info(">>>> Lock Card <<<< ");
            Result lockRel = commandWork(code + cardNO + POS_IC_SEQ + TIME);
            if(lockRel.codeType != 0) {
                Logger.info(">>>> FAIL: Lock Card");
                return;
            }

            String[] consumeData = new String[20];
            consumeData[0] = "CUOWU";

            analyzeConsume(lockRel.fdBytes, consumeData);
            if(consumeData[0].equals("AA00")) {
            	persitConsumeData(consumeData);//解析成功的数据才进行持久化工作
                Logger.info(">>>> SUCCESS: Lock Card");
            }else {
                Logger.info(">>>> FAIL: Lock Card");
            }
        } catch(Exception e) {
            Logger.warn(">>>> FAIL: Lock Card, catch exception");
        }
    }

    /**
     * 验证卡信息的合法性(黑名单卡,卡类型,物理卡类型,卡状态,卡失效日期,计次类型等)
     *
     * @param cardInfo 读出的卡信息
     * @return 0-验证成功,非0-验证失败
     */
	public static int verifiCardInfo(String[] cardInfo) {
		try {
			if (LocalContext.CACHE_BLACK_LIST.containsKey(Long.parseLong(cardInfo[2]))) {
				Logger.warn(">>>> WARN: In blacklist, card Number="+ cardInfo[2]);
				return IN_BLACK_LIST;
			}

			if (!LocalContext.CACHE_CONSUME.contains(cardInfo[4] + cardInfo[3])) {
				Logger.warn(">>>> WARN: Card can not consume.Physical type:"
						+ cardInfo[4] + ", logic type:" + cardInfo[3]);
				return NO_CONSUME_CARD;
			}

			if (cardInfo[9].equals("D007")) {
				String str2;
				int size = LocalContext.CARD_GREY_LIST.size();
				for (int i = 0; i <= size; i++) {
					String str = LocalContext.CARD_GREY_LIST.get(i);
					String str1 = str.substring(0, 2);
					if (str1.equals("01")) {
						str2 = str.substring(2, 10);
						if (str2.equals("D0070000")) {
							Logger.warn(">>>>> WARN: In greylist ");
							return IN_GREY_LIST;
						}
					}
				}
			}
		} catch (Exception e) {
			Logger.error("Exception : " + e.getMessage());
		}
		return SUCCESS;
	}

    /**
     * 向硬件设备发送签到指令
     *
     * @return 签到指令反馈结果
     */
    public static Result sign() {
        return commandWork("AA51");
    }

    /**
     * 读版本指令
     * @return 读版本指令,处理读模块版本业务
     */
    public static Result readVersion() {
        return commandWork("AA54");
    }
    
    /**
     * 向设备发送签到认证指令
     *
     * @return 反馈信息
     */
    public static Result signCert() {
        Result result = commandWork("AA52" + LocalContext.CACHE_ENC_TEXT + LocalContext.CACHE_MAC_BUF);
        String signRespCode = new String(result.fdBytes, 5, 4); // 应答代码

        if(signRespCode.equals("AA00")) {
            Logger.info(">>>> SUCCESS: sign certification.Code=AA00");
        } else if(signRespCode.equals("AA12")) {
            Logger.info(">>>> FAIL: sign certification. Code=AA12");
            byte[] remainTimes = new byte[] { result.fdBytes[9], result.fdBytes[10]};
            Logger.info(">>>> WARN: The remaining number of certificaiton is " + new String(remainTimes));
        }

        return result;
    }

    /**
     * 签退复位指令
     *
     * @return 反馈结果
     */
    public static Result signOutReset() {
        return commandWork("AA53");
    }

    /**
     * 命令处理工作,包括命令生成、设备交互以及设备反馈校验
     *
     * @param cmd 命令
     * @return 设备反馈
     */
    public static Result commandWork(String cmd) {
        /** 初始化命令数据*/
        byte[] signInCmd = LocUtils.initCommand(cmd.toCharArray());

        /** 与硬件设备通讯*/
        Result result  = rs232Work(signInCmd);
        if(result.bytesCode != 0) {
            Logger.info(">>>> FAIL: Receive device data");
            return result;
        }

        /** 校验反馈报文的格式 */
        int check = LocUtils.checkCommand(result.fdBytes);
        if(check != 0) {
            Logger.info(">>>> FAIL: Check command data:check=" + check);
            return new Result(Result.BYTESCODE, null, check, null, -1);
        }

        /** 报文反馈应答码判断*/
        byte[] signFeedback = result.fdBytes;
        String signRespCode = new String(signFeedback, 5, 4); // 应答代码
        if(!signRespCode.equals("AA00")) {
            Logger.info(">>>> FAIL: Response fail, code=" + signRespCode);
            return new Result(Result.HDCODE, null, -1, signRespCode, -1);
        }

        return result;
    }

    /**
     * 串口操作,包括发送字节流到串口和接收串口返回的字节流
     *
     * @param cmd 发送的字节流
     * @return 串口返回的结果封装
     */
    private static Result rs232Work(byte[] cmd) {
    	byte[] feedback = null;
		int open = rs232.open();
		if (open != 0) {
			Logger.warn(">>>> FAIL: open com,code=" + open);
			return new Result(Result.BYTESCODE, null, open, null, -1);
		}
		try {
			rs232.write(cmd);// 发送命令到端口
			feedback = rs232.read(IO_TIMEOUT);// 读取端口反馈命令
		} catch (Exception e) {

		} finally {
			int close = rs232.close();
			if (close != 0)
				Logger.warn(">>>> FAIL: close com,code=" + close);
		}

		if (feedback == null)
			return new Result(Result.HARDWARE, null, 302, null, -1); // 反馈超时

		return new Result(Result.CODESUCCESS, feedback, 0, null, -1);
	}

    /**
     * 根据步长分解list
     *
     * @param list 原始list
     * @param step 步长
     * @return 分解后的list集合,并且保证原始顺序
     */
    private static List<List<String>> splitsListByStep(List<String> list, int step) {
        if(list == null) throw new IllegalArgumentException("list is NULL");
        if(step <= 0) throw new IllegalArgumentException("step <= 0, error");

        List<List<String>> resultList = new ArrayList<List<String>>(list.size() % step + 1);
        int index = 0;
        int len = list.size();

        for(;(index + step) <= len; index+=step) {
            List<String> splist = new ArrayList<String>(step);
            for(int i = index; i < index + step; i++) {
                splist.add(list.get(i));
            }
            resultList.add(splist);
        }

        if(index < len) {
            List<String> splist = new ArrayList<String>(step);
            for(;index < len; index++) {
                splist.add(list.get(index));
            }
            resultList.add(splist);
        }

        return resultList;
    }
}
