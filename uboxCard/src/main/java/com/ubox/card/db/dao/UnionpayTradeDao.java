package com.ubox.card.db.dao;


import com.alibaba.fastjson.JSON;
import com.ubox.card.db.DbConst;
import com.ubox.card.util.logger.Logger;
import com.ubox.card.bean.db.UnionpayTradeObj.UnionpayTrade;

import java.util.*;

public class UnionpayTradeDao extends BaseDao {

    private static final UnionpayTradeDao instance = new UnionpayTradeDao();

    private UnionpayTradeDao() {
    }

    public static UnionpayTradeDao getInstance() {
        return instance;
    }

    @Override
    public String tableName() {
        return DbConst.TABLE_UNIONPAY_TRADE;
    }

	@SuppressWarnings("unchecked")
    @Override
    public int insertOne(Object rec) {
		synchronized(this) {
			if(!(rec instanceof UnionpayTrade)) {
				return DB_FAIL;
			}
			
    		if (records == null) {// 初始化未成功
    			init();
    			return DB_FAIL;
    		}
    		
    		String key = ((UnionpayTrade)rec).getId();
            records.put(key, (UnionpayTrade)rec);
            insertAll();
            
            return DB_SUCCESS;
		}
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public synchronized List<Object> queryLatestMaxN(int max) {
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
    				list.add(new UnionpayTrade(JSON.toJSONString(treeMap.get(key))));
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
