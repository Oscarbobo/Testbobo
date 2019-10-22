package com.ubox.card.device.bjszykt.pubwork;

import android.annotation.SuppressLint;

import com.ubox.card.core.WorkPool;
import com.ubox.card.util.logger.Logger;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PubUtils {
	
    /**
     * 正整数转换成字节数组(小端模式)
     *
     * @param digital 正整数
     * @param length 返回的字节数组长度
     * @return 小端字节数组
     */
    public static byte[] i2bLt(int digital, int length) {
        if(length <= 0) throw new IllegalArgumentException("length <= 0");

        byte[] bs = new byte[length];
        for(int i = 0, y = 0; (y < length) && (i < 4) ; i++, y++) {
            byte b = (byte)((digital >> (i * 8)) & 0xFF);
            bs[y] = b;
        }

        return bs;
    }

    /**
     * 正整数转换成字节数组(大端模式)
     *
     * @param digtial 正整数
     * @param length 返回的字节数组长度
     * @return 大端字节数组
     */
    public static byte[] i2bLg(int digtial, int length) {
        if(length <= 0 || length > 4) throw new IllegalArgumentException("length must in 1~4");

        byte[] bs = new byte[length];
        for(int i = 0, y = length - 1; (y >= 0) && (i < 4); i++, y--)
            bs[y] = (byte)(digtial >> (i * 8));

        return bs;
    }

    /**
     * 字节数组转换成正整数(小端模式)
     *
     * @param barr 字节数组
     * @param length 转换的字节长度
     * @return 正整数
     */
    public static int b2iLt(byte[] barr, int length) {
        if(barr == null) throw new IllegalArgumentException("barr is NULL");
        if(length < 0) throw new IllegalArgumentException("length less than 0");

        int r = 0;
        int len = barr.length < length ? barr.length : length;
        for(int i = 0; i < len; i++)
            r |= (barr[i] & 0xFF) << (i * 8);
        return r;
    }

    /**
     * 网络报文计算计算CRC
     *
     * @param i CRC原始值
     * @param length CRC字节数组长度
     * @return CRC值
     */
    public static byte[] I2CRC(int i, int length) {
        if(i < 0 || length < 0)
            throw new IllegalArgumentException("params must large than 0");
        if(String.valueOf(i).length() > length * 2)
            throw new IllegalArgumentException("length is too small");

        byte[] bcd  = new byte[length];
        for(int bi = 0, bilen = bcd.length; bi < bilen; bi ++)
            bcd[bi] = (byte)0xFF; // 数组初始化成FF

        byte[] i2b = i2bLg(i, 2);
        System.arraycopy(i2b, 0, bcd, 0, 2);

        return bcd;
    }

    /**
     * 字节数组转换成HEX字符串
     *
     * @param arr 字节数组
     * @return HEX字符串
     */
    public static String BA2HS(byte[] arr) {
        if(arr == null)  throw new IllegalArgumentException("arg is NULL");

        StringBuilder sb = new StringBuilder(arr.length * 2);
        for(int b : arr) {
            String tmp = Integer.toHexString(b & 0xFF);
            if(tmp.length() < 2) sb.append('0');
            sb.append(tmp);
        }

        return sb.toString();
    }

    /**
     * HEX字符串转换成字节数组.HEX字符串如果是奇数,则在尾补'0'成偶数
     *
     * @param HEXStr HEX字符串
     * @return 字符数组
     */
    public static byte[] HS2BA(String HEXStr) {
        if(HEXStr == null) throw new IllegalArgumentException("arg is NULL");
        if(HEXStr.length() == 0) return new byte[0];

        StringBuilder sb = new StringBuilder(HEXStr);
        if(!(HEXStr.length() % 2 == 0))  sb.append('0');

        char[] hs = sb.toString().toCharArray();
        byte[] bArr = new byte[hs.length / 2];

        for(int i = 0, len = hs.length; i < len; ++i) {
            byte high = (byte)Character.digit(hs[i], 16);
            byte low  = (byte)Character.digit(hs[++i], 16);

            bArr[i/2] = (byte)((high << 4) | low);
        }

        return bArr;
    }


    /**
     * CRC算法
     */
    public static char pubCalCRC(char[] ptr) {
        int i;
        char crc=0;

        for(char p : ptr) {
            for(i = 128; i != 0; i /= 2) {
                if((crc & 0x8000) != 0) {
                    crc *= 2 ;
                    crc ^= 0x1021;
                }else  crc *= 2 ;

                if((p & i) != 0) crc ^= 0x1021;
            }
        }

        return (char)(crc & 0xFFFF);
    }

    /**
     * 生成系统时间字符串
     *
     * @return 系统时间,格式 yyyyMMddhhmmss
     */
    @SuppressLint("SimpleDateFormat") 
    public static String generateSysTime() {
    	Logger.info("yyyyMMddHHmmss : "+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    /**
     * 文件句柄获取或者创建新的文件句柄
     *
     * @param filePath 文件路径
     * @param logFileName 日志里面文件名称
     * @return 文件句柄,null则失败
     */
    public static File fileGetOrCreate(String filePath, String logFileName) {
        File file;

        try{
            file = new File(filePath);
            if(!file.exists()) {
                if(!file.createNewFile()){
                    Logger.warn(">>>>FAIL:Create " + logFileName + " fail");
                    return null;
                }else Logger.info(">>>>SUCCESS: Create " + filePath + " success");
            }

            if(!file.isFile()) {
                Logger.warn(">>>> " + logFileName + " not a file.");

                if(file.delete()) Logger.info(">>>>SUCCESS: " + filePath + " delete success");
                else Logger.info(">>>>FAIL: " + filePath + " delete fail");

                return null;
            }

        } catch(IOException e) {
            Logger.error(logFileName + " fileHandler error", e);
            file = null;
        }

        return file;
    }

    /**
     * 按行读取指定文件内容
     *
     * @return 读取值
     */
    public static List<String> readFileByLine(String filePath) {
        List<String> linkList = new LinkedList<String>();
        try {
            File file = PubUtils.fileGetOrCreate(filePath, "增量黑名单文件");

            BufferedReader br = new BufferedReader(new FileReader(file));
            for(String tmp; (tmp = br.readLine()) != null; )  {
                if(tmp.trim().length() != 0)linkList.add(tmp);
            }
            br.close();
        } catch(Exception e) {
            Logger.error(">>>> Read file by Line Error. File Path:" + filePath, e);
        }

        return linkList;
    }

    // 文件写入锁
    private static final Object FILE_OBJECT_LOCK = new Object();

    /**
     * 写入参数数据到文件
     *
     * @param file 数据文件
     * @param isInc 是否为增量数据
     * @param data 参数数据
     * @param step 数据单位长度
     * @return 0-成功,非0-失败
     */
    public static int writeData2File(final File file, final boolean isInc, final String data, final int step) {
        if(file == null || data == null) throw new IllegalArgumentException("NULL params");
        if(step <= 0) throw new IllegalArgumentException("error: step <= 0");

        WorkPool.executeTask(new Runnable() {
            @Override
            public void run() {
                synchronized(FILE_OBJECT_LOCK) {
                    BufferedWriter bw = null;
                    try {
                        bw = new BufferedWriter(new FileWriter(file, isInc));

                        int length = data.length();
                        int index = 0;
                        String tmp;
                        while((index + step) <= length) {
                            tmp = data.substring(index, index + step);
                            index += step;
                            bw.write(tmp + LocalContext.LINE_SEPARATOR);
                        }

                        if(index < length) bw.write(data.substring(index) + LocalContext.LINE_SEPARATOR);
                    } catch(IOException e) {
                        Logger.error(">>>>FAIL: Persist ERROR.", e);
                    } finally {
                        if(bw != null) try {
                            bw.close();
                        } catch(IOException e) {
                            Logger.error(">>>>FAIL: Persist ERROR.", e);
                        }
                    }
                }
            }
        });

        return 0;
    }

    /**
     * configs文件里面持久化 "键-值"对
     *
     * @param kvs 键值对集合
     * @return 0-成功;非0-失败
     */
    public static synchronized int configsPerisit(Map<String, String> kvs) {
        if(kvs == null) throw new IllegalArgumentException("kvs is NULL");

        try {
            File configsFile    = PubUtils.fileGetOrCreate(configsPath, "configs.ini");

            // 新增或则更新相同key数据
            PropertyResourceBundle prb = new PropertyResourceBundle(new FileInputStream(configsFile));
            for(String key : prb.keySet()) {
                if(!kvs.containsKey(key)) kvs.put(key, prb.getString(key));
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(configsFile));
            for(String key : kvs.keySet()) bw.write(key + "=" + kvs.get(key) + LocalContext.LINE_SEPARATOR);

            bw.close();
        } catch(IOException e) {
            Logger.error(">>>>FAIL: Persist Configs fail", e);
            return -1;
        }

        return 0;
    }

	private static final String configsPath  = LocalContext.workPath + File.separator + "configs.ini";
	
    /**
     * 从configs文件里通过key取value
     * @param key key值
     * @return value值.如果不存在,则返回null
     */
    public static synchronized String getFromConfigs(String key) {
        String value = null;
        try {
            File configsFile    = PubUtils.fileGetOrCreate(configsPath, "configs.ini");

            PropertyResourceBundle prb = new PropertyResourceBundle(new FileInputStream(configsFile));

	        value = prb.getString(key);
            if(value == null || value.trim().equals("")) {
                if(key.equals("POS_IC_SEQ")){ 
                	value = "00000000";
                }
            }
        } catch(Exception e) {
            Logger.error(">>>>FAIL: Get value from config fail.Key=" + key, e);
            value = null;
            if(key.equals("POS_IC_SEQ")) value = "00000000";
            if(key.equals("batchNO")) value = String.valueOf(LocalContext.batchNo==null?"1":(LocalContext.batchNo + 1));
        }
        return value;
    }

    /**
     * 获取configs文件里面的LimitTime
     *
     * @return limitTime的long值
     */
    @SuppressLint("SimpleDateFormat") 
    public static long getLimitTime() {
        String limitTimeStr = getFromConfigs("LimitTime");
        if(limitTimeStr == null) {
            Logger.warn(">>>> LimitTime not in configs.ini");
            return 86400000 + System.currentTimeMillis(); // 延迟一天
        }

        long limitTime;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Date limitTimeDate   = sdf.parse(limitTimeStr);
            limitTime = limitTimeDate.getTime();
        } catch(ParseException e) {
            Logger.warn(">>>> LimitTime parse Error. limitTimeStri = " + limitTimeStr);
            return 86400000 + System.currentTimeMillis(); // 延迟一天
        }

        return limitTime;
    }

}


