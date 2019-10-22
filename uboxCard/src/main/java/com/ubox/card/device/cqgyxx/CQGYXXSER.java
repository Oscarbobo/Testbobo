package com.ubox.card.device.cqgyxx;

import android.annotation.SuppressLint;

import com.ubox.card.core.serial.IcCom;
import com.ubox.card.util.RSCalcUtils;
import com.ubox.card.util.Utils;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@SuppressLint("DefaultLocale")
public class CQGYXXSER {

    private IcCom com = new IcCom(9600, IcCom.DATABITS_8, IcCom.STOPBITS_1, IcCom.PARITY_EVEN);

    static enum RESULT {SUCCESS, CANCEL, TIMEOUT, ERROR}

    ;

    /**
     * 1打开串口
     *
     * @return
     */
    public RESULT open() {
        try {
            com.open();
        } catch (Exception e) {
            Logger.error("open is exception. " + e.getMessage(), e);
            return RESULT.ERROR;
        }
        return RESULT.SUCCESS;
    }

    /**
     * 2关闭串口
     *
     * @return
     */
    public RESULT close() {
        try {
            com.close();
        } catch (Exception e) {
            Logger.error("close is exception. " + e.getMessage(), e);
            return RESULT.ERROR;
        }
        return RESULT.SUCCESS;
    }

//    public static byte lrc(byte[] cmd) {
//        byte b = (byte) 0x00;
//        if (cmd.length < 2) {
//            return b;
//        }
//        b = cmd[1];
//        for (int i = 1, j = cmd.length - 1; i < j; i++) {
//            b ^= cmd[i];
//        }
//        return b;
//    }
    /**
     * 3计算LRC
     *
     * @param cmd 命令指令
     */
    public static byte lrc(byte[] cmd) {
        byte b = (byte) 0x00;
        if (cmd.length < 2) {
            return b;
        }
        for (int i = 1; i < cmd.length - 1 ;i++) {
            b ^= cmd[i];
        }
        return b;
    }

    /*
    * 4,判断是否有卡
    * */
    @SuppressLint("DefaultLocale")
    public RESULT readCard(String nb) {

        byte[] sendBytes = {(byte) 0x02, (byte) 0x43, (byte) 0x53, (byte) 0x03, (byte) 0x13};
        sendBytes[sendBytes.length - 1] = lrc(sendBytes);

        byte[] receiveBytes;
        int timeout = 10;
        int sleepTime = 1000;//毫秒
        //计时开始
        long startTime = System.currentTimeMillis();
        try {
            receiveBytes = talkWith(sendBytes, timeout, sleepTime);
            Logger.info(">>> send readCard:" + Utils.toHex(sendBytes));


            //4.1校验头
            if ((byte) 0x02 != receiveBytes[0]) {
                Logger.error("receive read Card response's head is error. 0x02 != " + Utils.toHex1(receiveBytes[0]));
                return RESULT.ERROR;
            }

            //4.2校验尾
            if ((byte) 0x03 != receiveBytes[receiveBytes.length - 2]) {
                Logger.error("receive read Card response's end is error. 0x03 != " + Utils.toHex1(receiveBytes[receiveBytes.length - 2]));
                return RESULT.ERROR;
            }

            //4.2lrc校验
            if (receiveBytes[receiveBytes.length - 1] != lrc(receiveBytes)) {
                Logger.error("receive read Card response's end is error. 0x03 != " + Utils.toHex1(receiveBytes[receiveBytes.length - 2]));
                return RESULT.ERROR;
            }

            //4.3,计算发收的时间
            long endTime = System.currentTimeMillis();
            Logger.debug("read card time:" + (endTime - startTime) / 1000.00 + "s");

            //4.4,-->40代表成功返回
            if (Utils.toHex1(receiveBytes[4]).equals("40")) {
                return RESULT.SUCCESS;
            } else {
                if (Utils.toHex1(receiveBytes[4]).equals("00")) {
                    //无卡或读卡错误
                    return RESULT.ERROR;
                }
            }
        } catch (TimeoutException e) {
            Logger.error("read TimeoutException. " + e.getMessage(), e);
            return RESULT.ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return RESULT.ERROR;
        }
        return RESULT.ERROR;
    }

    /*
 * 5,读取卡的余额
 * */
    @SuppressLint("DefaultLocale")
    public Map<String, Integer> readCardMoney() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("code", -1);//0:成功，1:无卡,2:没有应答,其他都是错误
        try {
            Logger.info("read start ");
            long startTime = System.currentTimeMillis();
            byte[] sendBytes2 = {(byte) 0x02, (byte) 0x43, (byte) 0x52, (byte) 0x03, (byte) 0x12};
            sendBytes2[sendBytes2.length - 1] = lrc(sendBytes2);
            /* 与刷卡设备交互信息  */
            byte[] receiveBytes;
            int timeout = 2;
            int sleepTime = 1000;//毫秒
            try {
                receiveBytes = talkWith(sendBytes2, timeout, sleepTime);
            } catch (TimeoutException e) {
                Logger.error("read TimeoutException. " + e.getMessage(), e);
                map.put("code", 2);
                return map;
            } catch (Exception e) {
                Logger.error("read Exception. " + e.getMessage(), e);
                return map;
            }

            if (receiveBytes == null || receiveBytes.length < 4) {
                Logger.error("read receive bytes is null or length < 4. data:" + Utils.toHex(receiveBytes));
                return map;
            }
            //判断头
            if (receiveBytes[0] != (byte) 0x02) {
                Logger.error("read receive data STR is error. " + Utils.toHex1(receiveBytes[0]) + "!=02");
                return map;
            }
            //判断尾
            if (receiveBytes[receiveBytes.length - 2] != (byte) 0x03) {
                Logger.error("read receive data EXT is error. " + Utils.toHex1(receiveBytes[receiveBytes.length - 2]) + "!=03");
                return map;
            }
            //判断LRC
            byte lrc = lrc(receiveBytes);
            if (receiveBytes[receiveBytes.length - 1] != lrc) {
                Logger.error("read receive data LRC is error. " + Utils.toHex1(receiveBytes[receiveBytes.length - 1]) + "!=" + Utils.toHex1(lrc));
                return map;
            }



            //判断读取成功
            if (Utils.toHex1(receiveBytes[4]).equals("40")) {
//                if(ret[ret.length-3] != (byte)0x00){
//                    return map;
//                }
                //处理余额
                byte[] byteBalance = new byte[3];
                byteBalance = Arrays.copyOfRange(receiveBytes, 7, 10);//包含开头7,不包含结尾10
                String strBalance = Utils.toHex1(byteBalance[2]) + Utils.toHex1(byteBalance[1]) + Utils.toHex1(byteBalance[0]);
                int balance = Integer.parseInt(strBalance) * 10;
                Logger.info("balance:" + balance);
                map.put("code", 0);
                map.put("balance", balance);
            }

            Logger.info("read end. map:" + map.toString() + ",time:" + (System.currentTimeMillis() - startTime) / 1000.00 + "s");
            return map;
        } catch (Exception e) {
            Logger.error("read is exception. " + e.getMessage(), e);
            return map;
        }
    }

    /**
     * 6,扣款
     *
     * @param payMoney 金额,单位:分
     * @return 扣款结果
     */
    public Map<String, Object> pay(int payMoney) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("result", "01");
        try {
            Logger.info("pay start ");
            long startTime = System.currentTimeMillis();

            String strMoney = "0000000" + payMoney;
            strMoney = strMoney.substring(strMoney.length() - 1 - 6, strMoney.length() - 1);
            byte[] bMoney = Utils.decodeHex(strMoney);

            byte[] sendBytes3 = {(byte) 0x02, (byte) 0x43, (byte) 0x57, bMoney[2], bMoney[1], bMoney[0], (byte) 30, (byte) 30, (byte) 03, (byte) 37};
            sendBytes3[sendBytes3.length - 1] = lrc(sendBytes3);


			/* 6.1与刷卡设备交互信息*/
            byte[] receiveBytes;
            int timeout = 10;
            int sleepTime = 1000;
            try {
                receiveBytes = talkWith(sendBytes3, timeout, sleepTime);
            } catch (TimeoutException e) {
                Logger.error("pay TimeoutException. " + e.getMessage(), e);
                //return RESULT.TIMEOUT;
                return map;
            } catch (Exception e) {
                Logger.error("pay Exception. " + e.getMessage(), e);
                //return RESULT.ERROR;
                return map;
            }
            Logger.info("ret.length = " + receiveBytes.length);
            Logger.info("ret content(cost response:)" + DeviceUtils.byteArray2HASCII(receiveBytes));
            if (receiveBytes == null || receiveBytes.length != 6) {
                Logger.error("pay receive bytes is null or length !=6. data:" + Utils.toHex(receiveBytes));
                return map;
            }
            //6.2判断头
            if (receiveBytes[0] != (byte) 0x02) {
                Logger.error("pay receive data STR is error. " + Utils.toHex1(receiveBytes[0]) + "!=02");
                return map;
            }
            //6.3判断尾
            if (receiveBytes[receiveBytes.length - 2] != (byte) 0x03) {
                Logger.error("pay receive data EXT is error. " + Utils.toHex1(receiveBytes[receiveBytes.length - 2]) + "!=03");
                return map;
            }
            //6.4判断LRC
            byte lrc = lrc(receiveBytes);
            if (receiveBytes[receiveBytes.length - 1] != lrc) {
                Logger.error("pay receive data LRC is error. " + Utils.toHex1(receiveBytes[receiveBytes.length - 1]) + "!=" + Utils.toHex1(lrc));
                return map;
            }

            //6.5判断是否成功
            if(receiveBytes[receiveBytes.length-3] != (byte)0x30){
                Logger.error("pay receive data RSP is error. "+Utils.toHex1(receiveBytes[receiveBytes.length-3])+"!=30");
                return map;
            }
            //6.6扣款成功
            if (receiveBytes.length > 6 && receiveBytes[receiveBytes.length - 3] == 30) {
                // 1,账号,4个字节,转成int保存
                byte[] cardNO = new byte[4];
                System.arraycopy(receiveBytes, 4, cardNO, 0, cardNO.length);
                map.put("cardNO", Utils.bytesToInt(cardNO,0));

                //2终端机编号,2个字节,转成int保存
                byte[] cardDev = new byte[2];
                System.arraycopy(receiveBytes, 8, cardDev, 0, cardDev.length);
                map.put("cardDev", Utils.bytesToInt(cardDev,0));

                //3扣款流水号,4个字节,转成int保存
                byte[] costSeq = new byte[4];
                System.arraycopy(receiveBytes, 10, costSeq, 0, costSeq.length);
                map.put("costSeq", Utils.bytesToInt(costSeq,0));

                //4余额,3个字节,转成int保存
                //TODO
                byte[] cardBalance = new byte[3];
                System.arraycopy(receiveBytes,14,cardBalance,0,cardBalance.length);
                String tmpBalance = RSCalcUtils.BCD2StringBE(cardBalance[2]) + RSCalcUtils.BCD2StringBE(cardBalance[1]) + RSCalcUtils.BCD2StringBE(cardBalance[0]);
                map.put("cardBalance", Integer.parseInt(tmpBalance));//最后保存的是Integer,精确到分,而不是角

                //5扣款日期,3个字节,转成string保存
                byte[] costDate = new byte[3];
                System.arraycopy(receiveBytes, 17, costDate, 0, costDate.length);
                map.put("costDate", RSCalcUtils.BCD2StringBE(costDate));//将bite0x30变成30字符串

                //6扣款时间,3个字节,转成string保存
                byte[] costTime = new byte[3];
                System.arraycopy(receiveBytes, 20, costTime, 0, costTime.length);
                map.put("costTime", RSCalcUtils.BCD2StringBE(costTime));

            }

            Logger.info("pay end. time:" + (System.currentTimeMillis() - startTime) / 1000.00 + "s");

        } catch (Exception e) {
            Logger.error("pay is Exception. " + e.getMessage(), e);
            map.put("result", "01");
            return map;
        }
        return map;
    }


    /**
     * 4与设备通信
     *
     * @param cmd       发送给设备的命令
     * @param timeout   指令超时（秒）
     * @param sleepTime 延时（毫秒）
     * @return 接收到的命令
     * @throws Exception
     */
    private byte[] talkWith(byte[] cmd, int timeout, int sleepTime) throws Exception {
        try {
            OutputStream os = com.getOutputStream();
            InputStream is = com.getInputStream();

            //1通过串口发送数据给设备
            os.write(cmd);
            Logger.info("IO send: " + DeviceUtils.byteArray2HASCII(cmd));

            //2通过串口接收设备数据
            long startTime = System.currentTimeMillis();

            Logger.info("IO 读取: " + is.available());
            while (is.available() <= 0) {//此处修改把"<"改为"<="
                long endTime = System.currentTimeMillis();
                if (endTime - startTime >= timeout * 1000) {
                    Logger.warn("IO read time out. ");
                    throw new TimeoutException();
                }
            }

            Thread.sleep(sleepTime);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            while (is.available() > 0) {
                bao.write(is.read());
            }
            byte ret[] = bao.toByteArray();
            Logger.info("ret[] length = " + ret.length);
            Logger.info("返回指令IO receive: " + DeviceUtils.byteArray2HASCII(ret));
            //3,返回接收到的指令
            return ret;
        } catch (Exception e) {
            Logger.error("talkWith is exception. " + e.getMessage(), e);
            throw e;
        }
    }
}
