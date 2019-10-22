package com.ubox.card.db;

import com.ubox.card.config.CardConst;

import java.io.File;
import java.util.HashMap;

public class DbConst {
	/* * * * 数据文件名称定义  * * * */
	public static final String FILE_BRUSHCUPBOARDLOG       = "brushcupboardlog.db";
	public static final String FILE_MZTTRADE 		       = "mzttrade.db";
    public static final String FILE_UNIONPAY_TRADE         = "unionpay_trade.db";
    public static final String FILE_WHTTRADE 		       = "whttrade.db";
    public static final String FILE_QUICKPASSTRADE 		   = "quickpasstrade.db";
    public static final String FILE_HZSMKTRADE 		       = "hzsmktrade.db";
    public static final String FILE_YNYLSFTRADE 		   = "ynylsftrade.db";
    public static final String FILE_QCQTRADE 	   		   = "qcqtrade.db";
    
	/* * * * 数据库表名称定义  * * * */
	public static final String TABLE_BRUSHCUPBOARDLOG       = "mt_brush_cupboard_log";
	public static final String TABLE_MZTTRADE 		  		= "mt_mzttrade";
    public static final String TABLE_UNIONPAY_TRADE         = "mt_unionpay_trade";
    public static final String TABLE_WHTTRADE 		  		= "mt_whttrade";
    public static final String TABLE_QUICKPASSTRADE 		= "mt_quickpasstrade";
    public static final String TABLE_HZSMKTRADE				= "mt_hzsmktrade";
    public static final String TABLE_YNYLSFTRADE            = "mt_ynylsftrade";
    public static final String TABLE_QCQTRADE           	= "mt_qcqtrade";
    
	/* * * * 映射表 * * * * * * */
	private static final HashMap<String, String> MAP_FILE_TABLE = new HashMap<String, String>(); 
	private static final HashMap<String, String> MAP_TABLE_FILE = new HashMap<String, String>(); 
	
	/* * 数据文件字符集  * */
	public static final String CHARACTER_SET = "UTF-8";
	/* * 数据文件的跟路径  * */
	public static final String DB_BASE_PATH = CardConst.VCARD_PATH + File.separator + "DB/";
	/* * 数据文件中的数据分隔符  * */
	public static final String SEPARATOR_RECORD = "<<SEPARATOR>>";
	
	static {
		MAP_FILE_TABLE.put(FILE_BRUSHCUPBOARDLOG, TABLE_BRUSHCUPBOARDLOG);
		MAP_FILE_TABLE.put(FILE_MZTTRADE,         TABLE_MZTTRADE);
		MAP_FILE_TABLE.put(FILE_UNIONPAY_TRADE,   TABLE_UNIONPAY_TRADE);
		MAP_FILE_TABLE.put(FILE_WHTTRADE,         TABLE_WHTTRADE);
		MAP_FILE_TABLE.put(FILE_QUICKPASSTRADE,   TABLE_QUICKPASSTRADE);
		MAP_FILE_TABLE.put(FILE_HZSMKTRADE,   	  TABLE_HZSMKTRADE);
		MAP_FILE_TABLE.put(FILE_YNYLSFTRADE,   	  TABLE_YNYLSFTRADE);
		MAP_FILE_TABLE.put(FILE_QCQTRADE,   	  TABLE_QCQTRADE);
		
		MAP_TABLE_FILE.put(TABLE_BRUSHCUPBOARDLOG, FILE_BRUSHCUPBOARDLOG);
		MAP_TABLE_FILE.put(TABLE_MZTTRADE,         FILE_MZTTRADE);
		MAP_TABLE_FILE.put(TABLE_UNIONPAY_TRADE,   FILE_UNIONPAY_TRADE);
		MAP_TABLE_FILE.put(TABLE_WHTTRADE,         FILE_WHTTRADE);
		MAP_TABLE_FILE.put(TABLE_QUICKPASSTRADE,   FILE_QUICKPASSTRADE);
		MAP_TABLE_FILE.put(TABLE_HZSMKTRADE,       FILE_HZSMKTRADE);
		MAP_TABLE_FILE.put(TABLE_YNYLSFTRADE,       FILE_YNYLSFTRADE);
		MAP_TABLE_FILE.put(TABLE_QCQTRADE,       FILE_QCQTRADE);
	}
	
	public static boolean isContainDbFile(String fileName) {
		return MAP_FILE_TABLE.containsKey(fileName);
	}
	
	public static boolean isContainTableName(String tableName) {
		return MAP_TABLE_FILE.containsKey(tableName);
	}
	
	public static String getFileName(String tableName) {
		return isContainTableName(tableName) ? MAP_TABLE_FILE.get(tableName) : "";
	}
}


