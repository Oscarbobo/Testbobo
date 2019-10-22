package com.ubox.card.device.bjszykt.localwork;

import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.util.logger.Logger;

public class LocUtils {

    /**
     * 初始化命令
     *
     * @param data 数据内容
     * @return 命令数据
     */
    public static byte[] initCommand(char[] data) {
        if(data == null) throw new IllegalArgumentException("params is NULL");
        byte[] cmd = new byte[10 + data.length];

        /* 报文头 */
        cmd[0] = 0x02;

        /* 数据长度 */
        byte[] dataLen  = PubUtils.BA2HS(PubUtils.i2bLt(data.length, 2)).getBytes();
        System.arraycopy(dataLen, 0, cmd, 1, dataLen.length);

        /* 数据 */
        for(int d = 0, c = 5, dl = data.length; d < dl; d++, c++)
            cmd[c] = (byte)(data[d] & 0x00FF);

        /* 校验字节 */
        byte[] crc = PubUtils.BA2HS(PubUtils.i2bLt(PubUtils.pubCalCRC(data), 2)).getBytes();
        System.arraycopy(crc, 0, cmd, cmd.length - 5, crc.length);

        /* 报文尾 */
        cmd[cmd.length -1] = 0x03;

        return cmd;
    }

    /**
     * 校验命令的合法性
     *
     * @param cmd 命令字节数组
     * @return 0-校验成功,非0-校验失败
     */
    public static int checkCommand(byte[] cmd) {
        if(cmd == null) throw new IllegalArgumentException("params is NULL");
        if(cmd.length < 11) return 1; //报文长度最小是11

        /* 校验报文头 */
        if(cmd[0] != 0x02) return 2;

        /* 校验crc */
        byte[] dataLenHASCII = new byte[4];
        System.arraycopy(cmd, 1, dataLenHASCII, 0, 4);

        int dataLen = PubUtils.b2iLt(PubUtils.HS2BA(new String(dataLenHASCII)), 2);
        byte[] data = new byte[dataLen];
        System.arraycopy(cmd, 5, data, 0, dataLen);
        String dataCRC = PubUtils.BA2HS(PubUtils.i2bLt(PubUtils.pubCalCRC(new String(data).toCharArray()), 2));

        byte[] crc_byte = new byte[4];
        System.arraycopy(cmd, dataLen + 5, crc_byte, 0, 4);

        String orgCRC = new String(crc_byte);
        if(!orgCRC.equalsIgnoreCase(dataCRC)) {
        	Logger.warn("CRC ERROR: orgCRC=" + orgCRC + ", dataCRC=" + dataCRC);
        	return 3;
        }

        /* 校验报文尾 */
        if(cmd[cmd.length -1] != 0x03) return 4;

        return 0;
    }

}
