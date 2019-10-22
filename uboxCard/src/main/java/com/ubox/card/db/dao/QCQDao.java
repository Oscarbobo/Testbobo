package com.ubox.card.db.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.ubox.card.bean.db.QCQTradeObj.QCQTrade;
import com.ubox.card.db.DbConst;
import com.ubox.card.util.logger.Logger;

public class QCQDao extends BaseDao {

	private static final QCQDao instance = new QCQDao();
	
	private QCQDao() {
	}
	
	public static QCQDao getInstance() {
		return instance;
	}
	
	@Override
	public String tableName() {
		
		return DbConst.TABLE_QCQTRADE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int insertOne(Object record) {
		synchronized(this) {
    		if (!(record instanceof QCQTrade)) {// 参数为空或者参数不是需要的类型
    			return DB_FAIL;
    		}

    		if (records == null) {// 初始化未成功
    			init();
    			return DB_FAIL;
    		}

    		String key = ((QCQTrade) record).getBrushSeq();
    		records.put(key, (QCQTrade) record);

    		insertAll();

    		return DB_SUCCESS;
    	}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Object> queryLatestMaxN(int max) {
    	synchronized(this) {
    		if (records == null) {
    			init();
    			return new ArrayList<Object>();
    		}

    		int i = 1;
    		List<Object> list = new ArrayList<Object>();
    		if (records != null && records.size() != 0) {
    			TreeMap<String, Map<String, Object>> treeMap = (TreeMap) records;
    			Set<String> keySet = treeMap.descendingKeySet();
    			for (String key : keySet) {
    				list.add(new QCQTrade(JSON.toJSONString(treeMap.get(key))));
    				i++;
    				if (i > max) {
    					Logger.warn("records.size() > " + max + ".");
    					break;
    				}
    			}
    		} else {
    			Logger.warn(">>>> no record found, tableName=" + tableName());
    		}

    		return list;
    	}
	}

}
