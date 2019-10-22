package com.ubox.card.device.ynylsf.compos;

public class ComClass {

    private static final byte STX = 0x02; // 报文起始
    private static final byte ETX = 0x03; // 报文终止
    private static final byte FS  = 0x1C; // 域分隔符

    public static final byte ACK    = 0x06;  // 确认
    public static final byte NAK    = 0x15;  // 拒绝

    /**
     * 生成测试连接报文(TEST)
     *
     * @return 测试连接报文
     */
    public static byte[] genTEST() { return genPkt((byte)0x01, (byte)0x01, genID(), "99".getBytes()); }

    /**
     * 生成余额查询指令
     *
     * @return 余额查询指令
     */
    public static byte[] genBalanceInquiry() {
        /* 设置cont内容 */
        byte[] cont = new byte[29];

        System.arraycopy("04".getBytes(),                       0, cont, 0,  2);  cont[2]  = FS; // 指令代码
        System.arraycopy(YNYLSFContext.mchntid.getBytes(), 0, cont, 3,  15); cont[18] = FS; // 商户代码
        System.arraycopy(YNYLSFContext.posid.getBytes(),   0, cont, 19, 8);  cont[27]  = FS; // 终端号
        cont[28] = FS; // 商户中文名和商户英文名是备用字段,可以为空

        return genPkt((byte)0x03, (byte)0x01, genID(), cont);
    }

    /**
     * 生成消费指令
     *
     * @param n12 消费数据
     * @return 消费指令
     */
    public static byte[] genConsume(byte[] n12) {
        if(null == n12) throw new IllegalArgumentException("argument is NULL");
        if(n12.length != 12) throw new IllegalArgumentException("Consume byte array length != 12.");

        /* 设置cont内容 */
        byte[] cont = new byte[42];

        System.arraycopy("01".getBytes(),        0, cont, 0, 2); cont[2] = FS; // 指令代码
        System.arraycopy(YNYLSFContext.mchntid.getBytes(), 0, cont, 3,  15); cont[18] = FS; // 商户代码
        System.arraycopy(YNYLSFContext.posid.getBytes(),   0, cont, 19, 8);  cont[27] = FS; // 终端号
        cont[28] = cont[29] = FS; // 商户中文名和商户英文名是备用字段,可以为空

        System.arraycopy(n12, 0, cont, 30, 12); // 金额参数

        return genPkt((byte)0x03, (byte)0x01, genID(), cont);
    }

    /**
     * 生成结算指令
     *
     * @return 结算指令
     */
    public static byte[] genSettle() {
        /* 设置cont内容 */
        byte[] cont = new byte[29];

        System.arraycopy("52".getBytes(),                       0, cont, 0,  2);  cont[2]  = FS; // 指令代码
        System.arraycopy(YNYLSFContext.mchntid.getBytes(), 0, cont, 3,  15); cont[18] = FS; // 商户代码
        System.arraycopy(YNYLSFContext.posid.getBytes(),   0, cont, 19, 8);  cont[27] = FS; // 终端号
        cont[28] = FS; // 商户中文名和商户英文名是备用字段,可以为空

        return genPkt((byte)0x03, (byte)0x01, genID(), cont);
    }
    
    /**
     * 生成签到指令
     *
     * @return 签到指令
     */
    public static byte[] genSign() {
        /* 设置cont内容 */
        byte[] cont = new byte[29];

        System.arraycopy("51".getBytes(),                       0, cont, 0,  2);  cont[2]  = FS; // 指令代码
        System.arraycopy(YNYLSFContext.mchntid.getBytes(), 0, cont, 3,  15); cont[18] = FS; // 商户代码
        System.arraycopy(YNYLSFContext.posid.getBytes(),   0, cont, 19, 8);  cont[27] = FS; // 终端号
        cont[28] = FS; // 商户中文名和商户英文名是备用字段,可以为空

        return genPkt((byte)0x03, (byte)0x01, genID(), cont);
    }

    /**
     * 生成通信数据包
     *
     * @param path 数据流向属性
     * @param type 卡应用类型
     * @param id   数据包唯一标识
     * @param cont 数据正文
     * @return 通信数据包
     */
    public static byte[] genPkt(byte path, byte type, byte[] id, byte[] cont) {
        if(id == null || cont == null)
        	throw new IllegalArgumentException();
        if(id.length != 6)
        	throw new IllegalArgumentException("id.length != 6");

        byte[] pkt = new byte[13 + cont.length];  
        int pktLen = pkt.length;                     // 初始化通信报文
        byte[] len = new byte[]{(byte)(((pktLen - 5) / 256) & 0xFF), (byte)(((pktLen - 5) % 256) & 0xFF)}; // 初始化LEN

        /* 通信报文赋值 */
        pkt[0] = STX;                     // STX
        pkt[1] = len[0]; pkt[2] = len[1]; // LEN
        pkt[3] = path;                    // PATH
        pkt[4] = type;                    // TYPE
        System.arraycopy(id,   0, pkt,  5, 6);           // ID
        System.arraycopy(cont, 0, pkt, 11, cont.length); // CONT
        pkt[pktLen - 2] = ETX;            // EXT
        pkt[pktLen - 1] = calcLRC(pkt);   // LRC

        return pkt;
    }

    /**
     * 生成唯一标识
     *
     * @return ID
     */
    public static byte[] genID() { return new byte[] { 0x32, 0x30, 0x31, 0x33, 0x31, 0x32 }; }

    /**
     * 计算数据包的LRC
     *
     * @param packet 数据包
     * @return 校验结果
     */
    public static byte calcLRC(byte[] packet) {
        if(packet == null) throw new IllegalArgumentException();

        byte lrc   = packet[1];
        int  index = 2;
        while (index < packet.length - 1) {
            lrc ^= packet[index];
            index ++;
        }

        return lrc;
    }

    /**
     * 校验数据包
     *
     * @param packet 数据包
     * @return 0-成功;非0-失败
     */
    public static int checkPkt(byte[] packet) {
        if(packet == null) return 5; // 等待数据超时

        int pktLen  = packet.length;

        if(packet[0] != STX) return 1;          // 数据头验证
        if(packet[pktLen - 2] != ETX) return 2; // 数据尾验证

        int calcLen = ((packet[1] & 0xFF) << 8) + (packet[2] & 0xFF);
        if(calcLen + 5 != pktLen) return 3;     // 数据长度验证

        byte calc_lrc = calcLRC(packet);
        if(calc_lrc != packet[pktLen - 1]) return 4; // LRC验证

        return 0;
    }

    /**
     * 从packet中分离出cont信息
     *
     * @param packet 包信息
     * @return cont信息
     */
    public static byte[] isolatedCont(byte[] packet) {
        if(null == packet) 
        	throw new IllegalArgumentException();

        int contLen = packet.length - 13;
        byte[] cont = new byte[contLen];
        System.arraycopy(packet, 11, cont, 0, contLen);

        return cont;
    }

    /**
     * 从数据内容中解析出数据域
     * @param cont 数据内容
     * @return 数据域
     */
    public static byte[][] splitCont(byte[] cont) {
        int fsCount = 1;
        
        for(byte b : cont) 
        	if(b == FS) 
        		fsCount ++;
        
        byte[][] dataArea = new byte[fsCount][];

        int index = 0, dataLen = 0 , fc = 0;
        
        for(int i = 0; i < cont.length; i ++) {
            if(cont[i] != FS) 
            	dataLen ++;
            
            if(cont[i] == FS) {
                dataArea[fc] = new byte[dataLen];
                System.arraycopy(cont, index, dataArea[fc], 0, dataLen);

                dataLen = 0;
                index   = i + 1;
                fc++;
            }
        }
        
        dataArea[fc] = new byte[dataLen];
        System.arraycopy(cont, index, dataArea[fc], 0, dataLen);

        return dataArea;
    }
    
    /**
     * 解析脱机消费数据
     * @param cont
     * @return
     */
    public static byte[][] splitOffline(byte[] cont) {
        int fsCount = 1;
        for(byte b : cont) {
            if (b == FS) fsCount++;
        }
        byte[][] datas = new byte[fsCount][];

        int dataLen = 0;
        int index   = 0;
        int fc      = 0;
        for(int i = 0; i < cont.length && fc < 3; i++) {
            if(cont[i] != FS) {
                dataLen ++;
            } else {
                datas[fc] = new byte[dataLen];
                System.arraycopy(cont, index, datas[fc], 0, dataLen);

                dataLen = 0;
                index   = i + 1;
                fc ++;
            }
        }

        if(datas[0][0] == ACK) {
            datas[fc] = new byte[cont.length - index - 1];
            System.arraycopy(cont, index, datas[fc], 0, datas[fc].length);
        }

        return datas;
    }


}

