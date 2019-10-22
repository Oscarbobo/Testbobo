package com.ubox.card.db.dao;

import com.alibaba.fastjson.JSON;
import com.ubox.card.db.DbConst;
import com.ubox.card.util.StrUtil;
import com.ubox.card.util.logger.Logger;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("unchecked")
public abstract class BaseDao implements IDao {

	protected Map<String, Map<String, Object>> records;

	protected BaseDao() {}

	/**
	 * records和db文件内容同步
	 */
	@Override
    public int init() {
		synchronized(this) {
			try {
				records = readDbFile(DbConst.getFileName(tableName()));
				Logger.info(DbConst.getFileName(tableName()) + " init OK.");
				return DB_SUCCESS;
			} catch (Exception e) {
				Logger.error(">>>> init error: ", e);
				return DB_FAIL;
			}
		}
	}

	@Override
	public int deleteOne(String id) {
		synchronized(this) {
			if (!records.containsKey(id)) {
				Logger.warn(">>>> WARN table " + tableName() + " do not contain id=" + id);
				return DB_FAIL;
			}

			records.remove(id);

			return insertAll();
		}
	}
	
	/**
	 * 根据ID,删除多条数据
     *
	 * @param ids id集合
	 * @return 0-成功, 其他则失败
	 */
	public int deleteMany(List<String> ids) {
		synchronized(this) {
			for (String id : ids) {
				if (!records.containsKey(id)) {
					Logger.info(">>>>WARN table " + tableName() + " do not contain id=" + id);
				}
				records.remove(id);
			}

			return insertAll();
		}
	}

    /**
     * 全量插入数据
     *
     * @param records 全量数据
     * @return 0-成功,其他失败
     */
	protected int insertAll() {
		try {
			writeDbFile(DbConst.getFileName(tableName()), records);
			return DB_SUCCESS;
		} catch (Exception e) {
			Logger.error(">>>> insertAll error: ", e);
			return DB_FAIL;
		}
	}

    /**
     * 读取数据文件.List中的Map为json对象映射后所得
     *
     * @param fileName db数据文件名称
     * @return 数据文件内容
     */
	private Map<String, Map<String, Object>> readDbFile(String fileName) {
		if (!DbConst.isContainDbFile(fileName)) { // 校验文件名称是否合法
			Logger.warn(">>>> unexpect db file, fileName=" + fileName);
			return null;
		}

		if (!(new File(DbConst.DB_BASE_PATH).exists()) || (new File(DbConst.DB_BASE_PATH).isFile())) {
            new File(DbConst.DB_BASE_PATH).mkdirs(); // 若是数据文件目录不存在，则重新创建
        }

		File file = new File(DbConst.DB_BASE_PATH + fileName);
		if (!file.exists()) {
			Logger.warn(">>>> file not exist, fileName=" + fileName);
			try {
				file.createNewFile();
			} catch (IOException e) {
				Logger.error(">>>> create *.db file error.", e);
			}
		}

		/** 按行读取数据文件 数据文件的首行为表id数组 **/
		Map<String, Map<String, Object>> rs = new TreeMap<String, Map<String, Object>>();
        BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream( file), DbConst.CHARACTER_SET));

			String recordString ;
			while ((recordString = br.readLine()) != null) {
				String[] lineArray = recordString.split(DbConst.SEPARATOR_RECORD);
				if (lineArray.length == 0 || lineArray.length == 1) {
					Logger.warn(" [ " + tableName() + " ] " + " error db file record line , record=" + JSON.toJSONString(lineArray));
					continue;
				}

				String id = lineArray[0];
                try {
                    rs.put(id, (Map<String, Object>) JSON.parse(lineArray[1]));
                } catch (Exception e) {
                    Logger.warn(">>>> put records error: " + e.getLocalizedMessage());
                }
			}
			return rs;
		} catch (Exception e) {
            Logger.warn(">>>> Read db file error: " + e.getLocalizedMessage());
			return new TreeMap<String, Map<String, Object>>();
		} finally {
			try { if(br != null) br.close(); } catch (IOException e) {/* do nothing */ }
		}
	}

    /**
     * 全量写入数据到db数据表
     *
     * @param fileName db数据表
     * @param records 数据内容
     * @throws IOException 写入异常
     */
	private void writeDbFile(String fileName, Map<String, Map<String, Object>> records) {
		if (!DbConst.isContainDbFile(fileName)) { // 校验文件名称是否合法
			Logger.warn(">>>> unexpect db file, fileName=" + fileName);
			return ;
		}

		if (!(records instanceof Map)) { //  检查写入数据是否合法
			Logger.warn(" [ " + tableName() + " ] " + " unexpect in param (records), !(records instanceof Map)=" + !(records instanceof Map));
            return ;
		}

		StringBuilder sb = new StringBuilder();
		for (String key : records.keySet()) {
	        /** 格式化写入数据，按行写入，文件尾部追加 此处写入数据时必须要加入换行符 */
            sb.append(key + DbConst.SEPARATOR_RECORD + StrUtil.trim(JSON.toJSONString(records.get(key))) + "\n");
		}

		/** 写文件 */
		FileWriter fw = null;
		try {
			fw = new FileWriter(DbConst.DB_BASE_PATH + fileName);
			fw .write(new String(sb.toString().getBytes(), DbConst.CHARACTER_SET));
		} catch (Exception e) {
			Logger.error(">>>> write file error: ", e);
		} finally {
            try { if(fw != null) fw.close(); } catch (Exception e) { Logger.error(">>>> close *.db file error: ", e); }
		}
	}

}
