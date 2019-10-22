package com.ubox.card.device.bjszykt;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;

import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.config.CardConst;
import com.ubox.card.device.Device;
import com.ubox.card.device.bjszykt.localwork.LocWorker;
import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.device.bjszykt.pubwork.PubWorker;
import com.ubox.card.device.bjszykt.server.webapp.SimpleWebServer;
import com.ubox.card.device.bjszykt.threadwork.BJSZYKTthreadworker;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

@SuppressLint("SdCardPath")
public class BJSZYKT extends Device{
	public static Map<String, String> error = new HashMap<String, String>();
	public static Map<String, String> error2 = new HashMap<String, String>();
	@Override
	public void init() {
		/*
		 * 启动本地server NanoHTTPD22
		 */
		Logger.info("<<<<<<<<<<<<<<<<< init start");
		try {
			int port = 9999;
	        String host = null; // bind to all interfaces by default
	        String webappPath = "/mnt/sdcard/Ubox/resource/webapp/";
	        SimpleWebServer.start(host, port, webappPath);
	        Logger.info("NanoHTTPD start sucessful. "+host+":"+port);
		} catch (Exception e) {
			Logger.error("NanoHTTPD start fail. "+e.getMessage(), e);
		}
		
		BJSZYKTthreadworker.threadworkStart(); 
		
		error.put("01", "无效的消息类型码");
		error.put("02", "无效的消息版本");
		error.put("03", "通讯CRC错");
		error.put("04", "SAM卡号不存在");
		error.put("05", "SAM卡号已禁用");
		error.put("06", "SAM卡尚未签退");
		error.put("07", "通讯目标节点不可到达");
		error.put("08", "SAM卡尚未签到");
		error.put("09", "文件打开/读取失败");
		error.put("0a", "文件写入失败");
		error.put("0b", "文件不完整");
		error.put("0c", "无效的参数代码");
		error.put("0d", "参数不允许下载");
		error.put("0e", "其它未定义的错误");
		error.put("0f", "参数未下载");
		error.put("10", "程序未升级");
		error.put("11", "卡类型不匹配");
		error.put("12", "SAM卡与POS对应关系错");
		error.put("13", "SAM的单位号不匹配");
		error.put("14", "账户密码错");
		error.put("15", "卡片状态错");
		error.put("16", "账户状态错");
		error.put("17", "账户可用余额不足");
		error.put("18", "账户密码错误次数超限");
		error.put("19", "账户圈存次数超限");
		error.put("1a", "账户当次圈存金额超限");
		error.put("1b", "账户当日圈存金额超限");
		error.put("1c", "交易已冲正");
		error.put("1d", "交易已撤销");
		error.put("1e", "交易已退货");
		error.put("1f", "用户卡号不存在");
		error.put("20", "用户卡号已禁用");
		error.put("21", "单位授权额度余额不足");
		error.put("22", "账户已挂失");
		error.put("23", "卡片已退卡");
		error.put("24", "原始交易不存在");
		error.put("25", "未申请新的交易批次号");
		error.put("26", "不允许获取新的交易批次号");
		error.put("27", "交易未批上送不允许签退");
		error.put("28", "终端交易流水号异常");
		error.put("29", "需要更新终端交易流水号");
		error.put("2a", "终端已签到");
		error.put("2b", "日切中,不允许新的交易");
		error.put("2c", "总交易金额和总交易笔数不一致");
		error.put("2d", "交易类型错");
		error.put("2e", "账户不存在");
		error.put("2f", "账户有效期超限");
		error.put("30", "账户单笔交易金额超限");
		error.put("31", "账户当日消费金额超限");
		error.put("32", "发卡卡状态错");
		error.put("33", "无账户记录");
		error.put("34", "无可用账户");
		error.put("35", "交易状态错");
		error.put("36", "交易已经批上送，不允许撤销");
		error.put("37", "交易卡号与原交易卡号不一致");
		error.put("38", "交易SMA与原交易SAM号不一致");
		error.put("39", "交易金额不一致");
		error.put("3a", "证件类型错");
		error.put("3b", "证件号码错");
		error.put("3c", "不允许销户");
		error.put("3d", "不允许退现金");
		error.put("3e", "账户还有未解冻的金额，不允许销户");
		error.put("3f", "账户余额小于0，不允许销户");
		error.put("40", "卡内余额不足，需要进行自动圈存");
		error.put("41", "此卡不支持圈存功能");
		error.put("42", "SAM卡的商户号不匹配");
		error.put("43", "交易与原交易商户号不一致");
		error.put("44", "交易未清算，不能退货，请撤销");
		error.put("45", "卡状态异常，需要锁卡");
		error.put("46", "单张卡的账户个数超限");
		error.put("47", "交易金额大于原金额");
		error.put("48", "交易金额必须大于0");
		error.put("49", "卡押金非法");
		error.put("4a", "连接加密机失败");
		error.put("4b", "断开加密机失败");
		error.put("4c", "向加密机发送数据失败");
		error.put("4d", "接收加密机数据失败");
		error.put("4e", "数据长度错误");
		error.put("4f", "参数错误");
		error.put("50", "密钥索引不存在");
		error.put("51", "产生工作密钥失败");
		error.put("52", "加密报文失败");
		error.put("53", "报文MAC错误");
		error.put("54", "报文解密失败");
		error.put("55", "PIN密文转换失败");
		error.put("56", "PIN格式错误");
		error.put("57", "用户卡物理类型不可识别");
		error.put("58", "CPU卡MAC1校验失败");
		error.put("59", "CPU卡MAC2计算失败");
		error.put("5a", "M1卡密钥计算失败");
		error.put("5b", "POS不支持此业务");
		error.put("5c", "卡片超过最大宕账额");
		error.put("5d", "POS授权额度余额不足");
		error.put("5e", "POS当日累计充值额度超限");
		error.put("5f", "POS无充值额度");
		error.put("60", "柜员终端未日结");
		error.put("61", "柜员终端已日结");
		error.put("62", "柜员现金收入不平");
		error.put("63", "账户无密码可以修改");
		error.put("64", "终端状态类别错");
		error.put("65", "单位授权额度未设置");
		error.put("66", "终端授权额度未设置");
		error.put("67", "操作员不存在");
		error.put("68", "操作员状态错");
		error.put("69", "操作员密码错");
		error.put("6a", "操作员级别错");
		error.put("6b", "授权信息不存在");
		error.put("6c", "授权信息不匹配");
		error.put("6d", "不支持此类冲正业务");
		error.put("6e", "不能自己给自己授权");
		error.put("6f", "冻结金额大于账户可用余额");
		error.put("70", "解冻金额大于冻结金额");
		error.put("71", "账户有密码,不能增加账户密码");
		error.put("72", "此类型账户已存在不能重复开户");
		error.put("73", "操作数据库错");
		error.put("74", "账户挂失未启用");
		error.put("75", "账户不允许充值");
		error.put("76", "账户类型不存在");
		error.put("77", "终端商户授权未存在");
		error.put("78", "商户授权额度不足");
		error.put("79", "商户无授权额度");
		error.put("7a", "无法识别的调整类型");
		error.put("7b", "变更的新卡已经存在账户");
		error.put("7c", "用户卡认证信息非法");
		error.put("7d", "不允许退卡");
		error.put("7e", "应答与申请时的卡信息不匹配");
		error.put("7f", "申请的业务总中心尚未处理");
		error.put("80", "回送的数据可能错误");
		error.put("81", "选择文件，文件或密钥校验错误");
		error.put("82", "表示还可再试次数");
		error.put("83", "状态标志未改变");
		error.put("84", "写EEPROM不成功");
		error.put("85", "错误的长度");
		error.put("86", "CLA与线路保护要求不匹配");
		error.put("87", "无效的状态");
		error.put("88", "命令与文件结构不相容");
		error.put("89", "不满足安全状态");
		error.put("8a", "密钥被锁死");
		error.put("8b", "使用条件不满足");
		error.put("8c", "无安全报文");
		error.put("8d", "安全报文数据项不正确");
		error.put("8e", "数据域参数错误");
		error.put("8f", "功能不支持或卡中无MF或卡片已锁定");
		error.put("90", "文件未找到");
		error.put("91", "记录未找到");
		error.put("92", "文件无足够空间");
		error.put("93", "参数P1P2错");
		error.put("94", "在达到LE/LC字节之前文件结束,偏移量错误");
		error.put("95", "LE错误");
		error.put("96", "无效的CLA");
		error.put("97", "数据无效");
		error.put("98", "MAC错误");
		error.put("99", "应用已被锁定");
		error.put("9a", "金额不足");
		error.put("9b", "密钥未找到");
		error.put("9c", "所需的MAC不可用");
		error.put("9d", "CPU卡线路保护写MAC计算失败");
		error.put("9e", "交易后金额必须大于0");
		error.put("9f", "卡余额非法");
		error.put("a0", "空中圈存请求格式与配置不匹配");
		error.put("a1", "无法识别SW1SW2类型");
		error.put("a2", "请求顺序与临时流水不匹配，比预期值小");
		error.put("a3", "读发行文件一指令返回和读发行文件二指令返回必须一致 ");
		error.put("a4", "OVERAMT必须小于等于交易金额");
		error.put("a5", "APDU内部错误");
		error.put("a6", "黑名单卡已锁卡");
		error.put("b0", "不允许重复申请");
		error.put("b1", "不允许取消申请");
		error.put("b2", "不允许重复获取结果");
		error.put("b3", "退卡内余额超过最大宕账额");
		error.put("b4", "此卡已做过坏卡退卡类申请业务");
		error.put("b5", "缺少卡ENCKEY，需要上传卡ENCKEY");
		error.put("b6", "刮刮卡账户状态错");
		error.put("b7", "刮刮卡账户有效期超限");
		error.put("b8", "刮刮卡充值金额不匹配");
		error.put("b9", "不支持该业务模式");
		error.put("ba", "刮刮卡密码错误");
		error.put("bb", "刮刮卡密码错误次数超限");
		error.put("bc", "业务平台账户用户姓名不匹配");
		error.put("bd", "业务平台账户用户手机号不匹配");
		error.put("be", "刮刮卡账户状态未启用");
		error.put("bf", "刮刮卡账户状态未使用");
		error.put("c0", "刮刮卡账户状态已使用");
		error.put("c1", "刮刮卡账户状态已禁用");
		error.put("c2", "退卡退资交易退款方式错");
		error.put("c3", "退卡退资交易成本费方式错");
		error.put("c4", "验证平台签名错误");
		error.put("c5", "终端账户交易流水号异常");
		error.put("c6", "终端账户交易流水号需要更新");
		error.put("c7", "终端通讯易流水号异常");
		error.put("c8", "终端通讯流水号需要更新");
		error.put("c9", "获取时间超限");
		error.put("ca", "子卡已开户无法绑定");
		error.put("cb", "不存在绑定关系");
		error.put("cc", "卡片已经绑定");
		error.put("cd", "绑定数量超限");
		error.put("ce", "子卡不允许做该业务");
		error.put("cf", "集团资金池未配置");
		error.put("d0", "收款单不存在");
		error.put("d1", "缴费编号不存在");
		error.put("d2", "用户已缴费");
		error.put("d3", "缴费异常");
		error.put("d4", "卡片状态为非启用");
		error.put("d5", "交易卡号与原交易卡号相同");
		error.put("d6", "卡校验位错");
		error.put("d7", "不需更新累积信息");
		error.put("d8", "累积信息不一致");
		error.put("d9", "刮刮卡MAC验证失败");
		error.put("da", "自行车注册信息格式版本错");
		error.put("db", "数字签名错误");
		error.put("dc", "RSA公钥加密失败");
		error.put("dd", "RSA私钥解密失败");
		error.put("de", "无符合条件的订单记录");
		error.put("df", "可用的单张快充券额度不足");
		
		error2.put("AA01" , "指令错");
		error2.put("AA02" , "CRC检验错");
		error2.put("AA03" , "卡片不存在或已经移开");
		error2.put("AA04" , "多卡");
		error2.put("AA05" , "时间不合法");
		error2.put("AA06" , "卡状态不合法");
		error2.put("AA07" , "卡启用日期不合法");
		error2.put("AA08" , "卡有效日期不合法");
		error2.put("AA09" , "黑名单卡");
		error2.put("AA0a" , "钱包余额不合法");
		error2.put("AA0b" , "原余额小于钱包余额");
		error2.put("AA0c" , "卡片余额不足");
		error2.put("AA0d" , "押金非法");
		error2.put("AA0e" , "物理类型不允许");
		error2.put("AA0f" , "非同一张用户卡");
		error2.put("AA10" , "模块读取失败");
		error2.put("AA11" , "SAM卡上电失败");
		error2.put("AA12" , "SAM认证失败");
		error2.put("AA13" , "SAM解密失败");
		error2.put("AA14" , "SAM密匙验证失败");
		error2.put("AA15" , "SAM卡计算密匙失败");
		error2.put("AA16" , "SAM卡计算TAC失败");
		error2.put("AA17" , "SAM卡读卡失败");
		error2.put("AA18" , "工作密匙解密失败");
		error2.put("AA19" , "工作密匙加密失败");
		error2.put("AA1a" , "PIN加密失败");
		error2.put("AA1b" , "SAM卡不一致");
		error2.put("AA1c" , "指令参数错");
		error2.put("AA1d" , "模块存储失败");
		error2.put("AA1e" , "用户卡密匙认证失败");
		error2.put("AA1f" , "计数器损坏且恢复失败");
		error2.put("AA20" , "更新钱包失败");
		error2.put("AA21" , "更新有效期失败");
		error2.put("AA22" , "CPU密码认证错");
		error2.put("AA23" , "CPU读文件错");
		error2.put("AA24" , "圈存初始化失败");
		error2.put("AA25" , "CPU更新文件错");
		error2.put("AA26" , "有不完整交易");
		error2.put("AA27" , "更新卡交易计数错");
		error2.put("AA28" , "更新充值记录错");
		error2.put("AA29" , "更新卡状态错");
		error2.put("AA2a" , "更新黑名单失败");
		error2.put("AA2b" , "读块失败");
		error2.put("AA2c" , "写块失败");
		error2.put("AA2d" , "读余额错");
		error2.put("AA2e" , "交易金额不合法");
		error2.put("AA2f" , "实际余额不符");
		error2.put("AA30" , "SAM认证失败超次");
		error2.put("AA31" , "无需执行");
		error2.put("AA32" , "不能执行");
		error2.put("AA33" , "卡类型不合法");
		error2.put("AA34" , "交易已失败无需再试");
		error2.put("AA35" , "存在城铁不完整交易");
		error2.put("AA36" , "存在高速路不完整交易");
		error2.put("AA37" , "存在公交不完整交易");
		error2.put("AA38" , "清计次区错");
		error2.put("AA39" , "清消费区错");
		error2.put("AA30" , "清过程区错");
	}

	@Override
	public ExtResponse cardInfo(String json) {
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        
        Card card = new Card();
        card.setCardType(1);
        
        /** 判断市政一卡通的工作状态   **/
        if(LocalContext.VCARD_STATUS != LocalContext.VCRAD_READY) {
            ext.setResultCode(333);
            ext.setResultMsg("设备环境初始化中,不能消费");
            ext.getData().put("cards", new Card[]{card});

//            return Converter.genCardRepJSON(new Card[]{card}, ext);
            return ext;
        }
        
        /** 寻卡,判断卡是否放在读卡器上  **/
        int findcode = LocWorker.findCard(ext.getSerialNo()+"");
        if(findcode != LocWorker.SUCCESS) {// 寻卡失败
            ext.setResultCode(325);
            ext.setResultMsg("寻卡失败");

//            return Converter.genCardRepJSON(new Card[]{card}, ext);
            return ext;
        }
        
        /** 读取卡信息 */
        String[] cardInfo = LocWorker.readCard();
        if(cardInfo == null || !cardInfo[0].equals("AA00")) { // 读卡失败
            ext.setResultCode(325);
            ext.setResultMsg(error2.get(cardInfo[0]));

//            return Converter.genCardRepJSON(new Card[]{card}, ext);
            return ext;
        }
        
        /** 填充应答信息 **/
        card.setCardNo(cardInfo[2]);
        card.setCardBalance(PubUtils.b2iLt(PubUtils.HS2BA(cardInfo[8]), 4));
        
        ext.setResultCode(CardConst.SUCCESS_CODE);
        ext.setResultMsg("读卡成功");
        
//        return Converter.genCardRepJSON(new Card[]{card}, ext);
        return ext;
	}

	@Override
	public ExtResponse cost(String json) {
        ExtResponse ext  = Converter.cJSON2ExtRep(json);
        CostRep     rep  = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
        
        /** 判断市政一卡通的工作状态   **/
        if(LocalContext.VCARD_STATUS != LocalContext.VCRAD_READY) {
            ext.setResultCode(325);
            ext.setResultMsg("设备环境初始化中,不能消费");

            return Converter.genCostRepJSON(rep, ext);
        }
        
        if(PubWorker.batchNB == null){
        	ext.setResultCode(325);
            ext.setResultMsg("设备没有签到");

            return Converter.genCostRepJSON(rep, ext);
		}

        /** 寻卡,判断卡是否放在读卡器上  **/
		int findcode = LocWorker.findCard(ext.getSerialNo() + "");
		if (findcode != LocWorker.SUCCESS) {// 寻卡失败
			if (findcode == 415 || findcode == LocWorker.FIND_TIMEOUT) {
				return CancelProcesser.cancelProcess(json); // 撤销交易
			}
			ext.setResultCode(325);
			ext.setResultMsg("寻卡失败");

			return Converter.genCostRepJSON(rep, ext);
		}

        /** 读取卡信息 */
        String[] cardInfo = LocWorker.readCard();
        if(cardInfo == null || !cardInfo[0].equals("AA00")) { // 读卡失败
            ext.setResultCode(325);
            ext.setResultMsg("错误："+ error2.get(cardInfo[0]));

            return Converter.genCostRepJSON(rep, ext);
        }

        /** 卡信息验证,黑名单和可消费卡类型*/
        int verifyCode = LocWorker.verifiCardInfo(cardInfo);
        if(verifyCode != LocWorker.SUCCESS) { // 验卡失败
            if(verifyCode == LocWorker.IN_BLACK_LIST) { //黑名单卡
                ext.setResultCode(325);
                ext.setResultMsg("刷卡失败(黑名单卡)");

                LocWorker.lockCard(cardInfo[2]);// 锁卡
            } else if(verifyCode == LocWorker.NO_CONSUME_CARD) {
                ext.setResultCode(325);
                ext.setResultMsg("刷卡失败(非可消费卡类型)");
            } else if(verifyCode == LocWorker.IN_GREY_LIST) {
                ext.setResultCode(325);
                ext.setResultMsg("刷卡失败(灰名单卡)");
            } else {
                ext.setResultCode(325);
                ext.setResultMsg("刷卡失败(卡验证失败)");
            }

            return Converter.genCostRepJSON(rep, ext);
        }

        /** 卡余额判断 */
        int balance = PubUtils.b2iLt(PubUtils.HS2BA(cardInfo[8]), 4);
        if(balance < rep.getProduct().getSalePrice()) {
            ext.setResultCode(325);
            ext.setResultMsg("卡余额不足，余额:" + ((float)balance / 100.00F) + "元");

            return Converter.genCostRepJSON(rep, ext);
        }

		if (Utils.isCancel(ext.getSerialNo() + "")) {
			return CancelProcesser.cancelProcess(json); // 撤销交易
		}
        /** 脱机钱包消费 */
        String[] consume = LocWorker.offlineConsume(rep.getProduct().getSalePrice());

        if(consume == null || !consume[0].equals("AA00")) {
            ext.setResultCode(325);
            ext.setResultMsg("错误："+ error2.get(consume[0]));
        } else {
            Card card = rep.getCards()[0];

            card.setCardNo(cardInfo[2]);        // 用户卡号
            card.setPosId(consume[2]);          // SAM卡号是硬件设备的唯一标识
            rep.setThirdOrderNo(consume[4]);    // 交易顺序号是一卡通刷卡唯一序列号(设备绑定且唯一)
            card.setCardBalance(PubUtils.b2iLt(PubUtils.HS2BA(consume[5]), 4));    // 卡内余额

            ext.setResultCode(200);
            ext.setResultMsg("扣款成功");
        }

        return Converter.genCostRepJSON(rep, ext);
	}

}
