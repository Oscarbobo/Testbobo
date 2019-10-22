package com.ubox.card.device.hkkj;

import android.annotation.SuppressLint;

import com.ubox.card.core.serial.IcCom;
import com.ubox.card.util.Utils;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;


@SuppressLint("DefaultLocale")
public class HKKJER {
    private IcCom com = new IcCom();
    byte[] conflictPrevention = null;
    byte[] readCardReceive = null;
    Map<String, Object> map = new HashMap<String, Object>();


    static enum RESULT {SUCCESS, CANCEL, TIMEOUT, ERROR}

    /**
     * 1.打开串口
     */
    public RESULT open() {
        try {
            com.open();
        } catch (Exception e) {
            Logger.error("open is exception." + e.getMessage(), e);
            return RESULT.ERROR;
        }
        return RESULT.SUCCESS;
    }

    /**
     * 2.关闭串口
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

    /**
     * 位异或运算
     */
    private byte getXOR(byte[] data) {
        if (null == data) {
            throw new NullPointerException();
        }
        byte pb = data[0];
        for (int i = 1; i < data.length - 2; i++) {
            pb ^= data[i];
        }
        return pb;
    }

    /**
     * int 类型数据转两个byte数组
     *
     * @param v
     * @return
     */
    public static byte[] makeByte2(int v) {
        byte[] bytes = {(byte) ((v & 0xff00) >> 8), (byte) (v & 0xff)};
        return bytes;
    }


    /**
     * 寻卡
     */
    @SuppressLint("DefaultLocale")
    public RESULT findCard(String nb) {
        byte[] sendBytes = {
                (byte) 0xAA,
                (byte) 0x41,//操作码
                (byte) 0x00, (byte) 0x06,//数据长度
                (byte) 0x10,//卡型编号
                (byte) 0x00, (byte) 0x00,//分区地址
                (byte) 0x00, (byte) 0x00,//区内地址
                (byte) 0x01,//操作数据
                (byte) 0x00,//异或
                (byte) 0x03};
        sendBytes[sendBytes.length - 2] = getXOR(sendBytes);

        byte[] receiveBytes;
        int timeout = 20;//秒
        int sleepTime = 10;//毫秒
        //计时开始
        long startTime = System.currentTimeMillis();
        try {
            receiveBytes = talkWith(sendBytes, timeout, sleepTime);

            //4.1校验头
            if ((byte) 0x55 != receiveBytes[0]) {
                Logger.error("receive read Card response's head is error. 170 != " + Utils.toHex1(receiveBytes[0]));
                return RESULT.ERROR;
            }
            //4.2校验尾
            if ((byte) 0x03 != receiveBytes[receiveBytes.length - 1]) {
                Logger.error("receive read Card response's end is error. 03 != " + Utils.toHex1(receiveBytes[receiveBytes.length - 1]));
                return RESULT.ERROR;
            }

            //4.3校验XOR
            if (receiveBytes[receiveBytes.length - 2] != getXOR(receiveBytes)) {
                Logger.error("校验XOR出错！receive： " + receiveBytes[receiveBytes.length - 2] + "XOR:" + getXOR(receiveBytes));
                return RESULT.ERROR;
            }

            //4.4-->0代表成功返回,128读错误
            if (Utils.toHex1(receiveBytes[1]).equals("00")) {
                return RESULT.SUCCESS;
            } else if (Utils.toHex1(receiveBytes[1]).equals("128")) {
                //读卡错误
                return RESULT.ERROR;
            } else {
                //其他错误
                return RESULT.ERROR;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return RESULT.ERROR;
        }
    }

    /**
     * 防冲突
     */
    @SuppressLint({"DefaultLocale"})
    public byte[] conflictPrevention() {
        byte[] sendBytes = {
                (byte) 0xAA,
                (byte) 0x42,//操作码
                (byte) 0x00, (byte) 0x06,//数据长度
                (byte) 0x10,//卡型编号
                (byte) 0x00, (byte) 0x00,//分区地址
                (byte) 0x00, (byte) 0x00,//区内地址
                (byte) 0x00,//操作数据
                (byte) 0x00,//异或
                (byte) 0x03};
        sendBytes[sendBytes.length - 2] = getXOR(sendBytes);
        byte[] receiveBytes;
        int timeout = 20;//秒
        int sleepTime = 10;//毫秒
        //计时开始
        long startTime = System.currentTimeMillis();
        try {
            receiveBytes = talkWith(sendBytes, timeout, sleepTime);
            conflictPrevention = receiveBytes;

            /*byte[] bytes = Utils.subArray(receiveBytes, 4, 4);
            Logger.info("读到卡号:" + Utils.toHex(bytes)+"bytesToInt:"+bytesToInt(bytes,0));
            map.put("cardNo",bytesToInt(bytes,0));*/

            //4.1校验头
            if ((byte) 0x55 != receiveBytes[0]) {
                Logger.error("receive read Card response's head is error. 170 != " + Utils.toHex1(receiveBytes[0]));
                return null;
            }
            //4.2校验尾
            if ((byte) 0x03 != receiveBytes[receiveBytes.length - 1]) {
                Logger.error("receive read Card response's end is error. 03 != " + Utils.toHex1(receiveBytes[receiveBytes.length - 1]));
                return null;
            }

            //4.3校验XOR
            if (receiveBytes[receiveBytes.length - 2] != getXOR(receiveBytes)) {
                Logger.error("防冲突校验XOR出错！receive： " + receiveBytes[receiveBytes.length - 2] + "XOR:" + getXOR(receiveBytes));
                return null;
            }


            //4.4-->00代表成功返回
            if (Utils.toHex1(receiveBytes[1]).equals("00")) {
                return receiveBytes;
            } else {
                //其他错误
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 选卡
     */
    @SuppressLint({"DefaultLocale"})
    public RESULT selectCard() {
        byte[] sendBytes = {
                (byte) 0xAA,
                (byte) 0x43,//操作码
                (byte) 0x00, (byte) 0x09,//数据长度
                (byte) 0x10,//卡型编号
                (byte) 0x00, (byte) 0x00,//分区地址
                (byte) 0x00, (byte) 0x00,//区内地址
                (byte) conflictPrevention[4], (byte) conflictPrevention[5], (byte) conflictPrevention[6], (byte) conflictPrevention[7],//操作数据
                (byte) 0x00,//异或
                (byte) 0x03};
        sendBytes[sendBytes.length - 2] = getXOR(sendBytes);

        byte[] receiveBytes;
        int timeout = 20;//秒
        int sleepTime = 10;//毫秒
        //计时开始
        long startTime = System.currentTimeMillis();
        try {
            receiveBytes = talkWith(sendBytes, timeout, sleepTime);

            //4.1校验头
            if ((byte) 0x55 != receiveBytes[0]) {
                Logger.error("receive read Card response's head is error. 170 != " + Utils.toHex1(receiveBytes[0]));
                return RESULT.ERROR;
            }
            //4.2校验尾
            if ((byte) 0x03 != receiveBytes[receiveBytes.length - 1]) {
                Logger.error("receive read Card response's end is error. 03 != " + Utils.toHex1(receiveBytes[receiveBytes.length - 1]));
                return RESULT.ERROR;
            }

            //4.3校验XOR
            if (receiveBytes[receiveBytes.length - 2] != getXOR(receiveBytes)) {
                Logger.error("选卡校验XOR出错！receive： " + receiveBytes[receiveBytes.length - 2] + "XOR:" + getXOR(receiveBytes));
                return RESULT.ERROR;
            }


            //4.4-->00代表成功返回
            if (Utils.toHex1(receiveBytes[1]).equals("00")) {
                return RESULT.SUCCESS;
            } else {
                //其他错误
                return RESULT.ERROR;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return RESULT.ERROR;
        }

    }

    /**
     * 验证卡片密码
     */
    @SuppressLint({"DefaultLocale"})
    public RESULT verifyPassword() {
        byte[] sendBytes = {
                (byte) 0xAA,
                (byte) 0x5F,//操作码
                (byte) 0x00, (byte) 0x0C,//数据长度
                (byte) 0x13,//卡型编号
                (byte) 0x00, (byte) 0x00,//分区地址
                (byte) 0x00, (byte) 0x07,//区内地址
                (byte) 00,//A密码
                (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31, //6字节密码
                (byte) 0x00,
                (byte) 0x03};
        sendBytes[sendBytes.length - 2] = getXOR(sendBytes);
        byte[] receiveBytes;
        int timeout = 20;//秒
        int sleepTime = 10;//毫秒
        //计时开始
        long startTime = System.currentTimeMillis();
        try {
            receiveBytes = talkWith(sendBytes, timeout, sleepTime);


            //4.1校验头
            if ((byte) 0x55 != receiveBytes[0]) {
                Logger.error("receive read Card response's head is error. 170 != " + Utils.toHex1(receiveBytes[0]));
                return RESULT.ERROR;
            }
            //4.2校验尾
            if ((byte) 0x03 != receiveBytes[receiveBytes.length - 1]) {
                Logger.error("receive read Card response's end is error. 03 != " + Utils.toHex1(receiveBytes[receiveBytes.length - 1]));
                return RESULT.ERROR;
            }

            //4.3校验XOR
            if (receiveBytes[receiveBytes.length - 2] != getXOR(receiveBytes)) {
                Logger.error("验证卡片密码校验XOR出错！receive： " + receiveBytes[receiveBytes.length - 2] + "XOR:" + getXOR(receiveBytes));
                return RESULT.ERROR;
            }


            //4.4-->0代表成功返回
            if (Utils.toHex1(receiveBytes[1]).equals("00")) {
                return RESULT.SUCCESS;
            } else {
                //其他错误
                return RESULT.ERROR;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return RESULT.ERROR;
        }

    }

    /**
     * 读取卡信息：1扇区0块
     */
    @SuppressLint({"DefaultLocale"})
    public byte[] readCard() {
        byte[] sendBytes = {
                (byte) 0xAA,
                (byte) 0x61,//操作码
                (byte) 0x00, (byte) 0x05,//数据长度
                (byte) 0x13,//卡型编号
                (byte) 0x00, (byte) 0x00,//分区地址
                (byte) 0x00, (byte) 0x04,//区内地址
                (byte) 0x00,//异或
                (byte) 0x03};
        sendBytes[sendBytes.length - 2] = getXOR(sendBytes);
        byte[] receiveBytes;
        int timeout = 20;//秒
        int sleepTime = 10;//毫秒
        //计时开始
        long startTime = System.currentTimeMillis();
        try {
            receiveBytes = talkWith(sendBytes, timeout, sleepTime);
            readCardReceive = receiveBytes;
            //截取卡号
            byte[] bytes=Utils.subArray(receiveBytes,6,14);

            Logger.info("截取卡号:" + DeviceUtils.byteArray2HASCII(bytes));
            map.put("cardNO",DeviceUtils.byteArray2HASCII(bytes));
            //4.1校验头
            if ((byte) 0x55 != receiveBytes[0]) {
                Logger.error("receive read Card response's head is error. 170 != " + Utils.toHex1(receiveBytes[0]));
                return null;
            }
            //4.2校验尾
            if ((byte) 0x03 != receiveBytes[receiveBytes.length - 1]) {
                Logger.error("receive read Card response's end is error. 03 != " + Utils.toHex1(receiveBytes[receiveBytes.length - 1]));
                return null;
            }

            //4.3校验XOR
            if (receiveBytes[receiveBytes.length - 2] != getXOR(receiveBytes)) {
                Logger.error("读卡校验XOR出错！receive： " + receiveBytes[receiveBytes.length - 2] + "XOR:" + getXOR(receiveBytes));
                return null;
            }
            //4.4-->00代表成功返回
            if (Utils.toHex1(receiveBytes[1]).equals("00")) {
                return receiveBytes;
            } else {
                //其他错误
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取卡信息：1扇区1块
     */
    @SuppressLint({"DefaultLocale"})
    public byte[] readCardInfo() {
        byte[] sendBytes = {
                (byte) 0xAA,
                (byte) 0x61,//操作码
                (byte) 0x00, (byte) 0x05,//数据长度
                (byte) 0x13,//卡型编号
                (byte) 0x00, (byte) 0x00,//分区地址
                (byte) 0x00, (byte) 0x05,//区内地址
                (byte) 0x00,//异或
                (byte) 0x03};
        sendBytes[sendBytes.length - 2] = getXOR(sendBytes);
        byte[] receiveBytes;
        int timeout = 20;//秒
        int sleepTime = 10;//毫秒
        //计时开始
        long startTime = System.currentTimeMillis();
        try {
            receiveBytes = talkWith(sendBytes, timeout, sleepTime);
            readCardReceive = receiveBytes;
            //截取卡号
            byte[] bytes=Utils.subArray(receiveBytes,4,16);
            byte[] bytes1=Utils.subArray(receiveBytes,4,2);
            map.put("test",DeviceUtils.byteArray2HASCII(bytes1));

            Logger.info("截取1扇区1块:" + DeviceUtils.byteArray2HASCII(bytes));
            map.put("cardTemporary",DeviceUtils.byteArray2HASCII(bytes));
            //4.1校验头
            if ((byte) 0x55 != receiveBytes[0]) {
                Logger.error("receive read Card response's head is error. 170 != " + Utils.toHex1(receiveBytes[0]));
                return null;
            }
            //4.2校验尾
            if ((byte) 0x03 != receiveBytes[receiveBytes.length - 1]) {
                Logger.error("receive read Card response's end is error. 03 != " + Utils.toHex1(receiveBytes[receiveBytes.length - 1]));
                return null;
            }

            //4.3校验XOR
            if (receiveBytes[receiveBytes.length - 2] != getXOR(receiveBytes)) {
                Logger.error("读卡校验XOR出错！receive： " + receiveBytes[receiveBytes.length - 2] + "XOR:" + getXOR(receiveBytes));
                return null;
            }
            //4.4-->00代表成功返回
            if (Utils.toHex1(receiveBytes[1]).equals("00")) {
                return receiveBytes;
            } else {
                //其他错误
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 写数据
     *
     * @param payMoney 金额,单位:分
     * @return 扣款结果
     */
    @SuppressLint("DefaultLocale")
    public RESULT pay(int payMoney) {

        try {
            Logger.info(">>>pay start<<< ");
            byte[] bMoney = readCardReceive;
            //截取卡余额
            byte[] balanceY = {(byte) bMoney[16], (byte) bMoney[17], (byte) bMoney[18],(byte) bMoney[19]};
            System.out.println("余额：" + byteToInt2(balanceY) + "分");
            //计算扣款

            int realMoney = byteToInt2(balanceY) - payMoney;

            //扣款后卡余额（元）
            DecimalFormat df = new DecimalFormat("0.00");
            map.put("cardBalance",df.format((float)realMoney/100));
            if (realMoney < 0) {
                return RESULT.CANCEL;
            }
            //计算要写入的金额
            byte[] sendMoney = intToByteArray(realMoney);
            byte[] sendBytes = {
                    (byte) 0xAA,
                    (byte) 0x60,//操作码
                    (byte) 0x00, (byte) 0x15,//数据长度
                    (byte) 0x13,//卡型编号
                    (byte) 0x00, (byte) 0x00,//分区地址
                    (byte) 0x00, (byte) 0x04,//区内地址
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, sendMoney[0], sendMoney[1], sendMoney[2], sendMoney[3],//要写入的16字节数据
                    (byte) 0x00,//异或
                    (byte) 0x03};
            sendBytes[sendBytes.length - 2] = getXOR(sendBytes);

            /* 与刷卡设备交互信息*/
            byte[] receiveBytes;
            int timeout = 20;
            int sleepTime = 1000;
            try {
                receiveBytes = talkWith(sendBytes, timeout, sleepTime);
            } catch (TimeoutException e) {
                Logger.error("pay TimeoutException. " + e.getMessage(), e);
                return RESULT.TIMEOUT;
            } catch (Exception e) {
                Logger.error("pay Exception. " + e.getMessage(), e);
                return RESULT.ERROR;
            }
            Logger.info("ret.length = " + receiveBytes.length);
            Logger.info("ret content(cost response:)" + DeviceUtils.byteArray2HASCII(receiveBytes));
            if (receiveBytes == null || receiveBytes.length != 6) {
                Logger.error("pay receive bytes is null or length !=6. data:" + Utils.toHex(receiveBytes));
                return RESULT.ERROR;
            }

            //4.1校验头
            if ((byte) 0x55 != receiveBytes[0]) {
                Logger.error("receive read Card response's head is error. 170 != " + Utils.toHex1(receiveBytes[0]));
                return RESULT.ERROR;
            }
            //4.2校验尾
            if ((byte) 0x03 != receiveBytes[receiveBytes.length - 1]) {
                Logger.error("receive read Card response's end is error. 03 != " + Utils.toHex1(receiveBytes[receiveBytes.length - 1]));
                return RESULT.ERROR;
            }

            //4.3校验XOR
            if (receiveBytes[receiveBytes.length - 2] != getXOR(receiveBytes)) {
                Logger.error("读卡校验XOR出错！receive： " + receiveBytes[receiveBytes.length - 2] + "XOR:" + getXOR(receiveBytes));
                return RESULT.ERROR;
            }
            //4.4-->00代表成功返回
            if (Utils.toHex1(receiveBytes[1]).equals("00")) {
                return RESULT.SUCCESS;
            } else {
                //其他错误
                return RESULT.ERROR;
            }

        } catch (Exception e) {
            Logger.error("pay is Exception. " + e.getMessage(), e);
            return RESULT.ERROR;
        }

    }

    /**
     * 6.与设备通信
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
            //os.flush();
            Logger.info("IO send: " + DeviceUtils.byteArray2HASCII(cmd));

            //2通过串口接收设备数据
            long startTime = System.currentTimeMillis();

            Logger.info("IO 读取: " + is.available());
            while (is.available() <= 0) {
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



    /**
     * 字节数组转int
     * @param b
     * @return
     */

    public static int byteToInt2(byte[] b) {

        int mask = 0xff;
        int temp = 0;
        int n = 0;
        for (int i = 0; i < b.length; i++) {
            n <<= 8;
            temp = b[i] & mask;
            n |= temp;
        }
        return n;
    }


    /**
     * int转字节数组
     * @param a
     * @return
     */

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
    public static void main(String[] args){
        byte[] sendBytes = {
                (byte) 0xAA,
                (byte) 0x42,//操作码
                (byte) 0x00, (byte) 0x06,//数据长度
                (byte) 0x10,//卡型编号
                (byte) 0xB1, (byte) 0x52,//分区地址
                (byte) 0x58, (byte) 0x5c,//区内地址
                (byte) 0x00,//操作数据
                (byte) 0x00,//异或
                (byte) 0x03,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};

        //System.arraycopy(sendBytes, 5, bytes, 0, 4);
        byte[] bytes = Utils.subArray(sendBytes, 5, 4);
        System.out.println(bytes);

    }
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset+1] & 0xFF)<<8)
                | ((src[offset+2] & 0xFF)<<16)
                | ((src[offset+3] & 0xFF)<<24));
        return value;
    }

    public static int byteToInt(byte[] b) {
        int s = 0;
        int s0 = b[0] & 0xff;// 最低位
        int s1 = b[1] & 0xff;
        int s2 = b[2] & 0xff;
        int s3 = b[3] & 0xff;
        s3 <<= 24;
        s2 <<= 16;
        s1 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }

    public static String toHexString(String s)
    {
        String str="";
        for
        (int i=0;i<s.length();i++)
        {
            int ch = (int)s.charAt(i);
            String s4
                    = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

}