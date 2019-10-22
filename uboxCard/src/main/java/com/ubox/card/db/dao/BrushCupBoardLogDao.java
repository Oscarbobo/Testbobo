package com.ubox.card.db.dao;

import com.alibaba.fastjson.JSON;
import com.ubox.card.bean.db.BrushCupBoardLogObj.BrushCupBoardLog;
import com.ubox.card.db.DbConst;
import com.ubox.card.util.logger.Logger;

import java.util.*;

public final class BrushCupBoardLogDao extends BaseDao {

	private static final BrushCupBoardLogDao instance = new BrushCupBoardLogDao();

	private BrushCupBoardLogDao() {
	}

	public static BrushCupBoardLogDao getInstance() {
		return instance;
	}

	@Override
	public String tableName() {
		return DbConst.TABLE_BRUSHCUPBOARDLOG;
	}

    @SuppressWarnings("unchecked")
	@Override
	public int insertOne(Object record) {
    	synchronized(this) {
    		if (!(record instanceof BrushCupBoardLog)) {// 参数为空或者参数不是需要的类型
    			return DB_FAIL;
    		}

    		if (records == null) {// 初始化未成功
    			init();
    			return DB_FAIL;
    		}

    		String key = ((BrushCupBoardLog) record).getId();
    		records.put(key, (BrushCupBoardLog) record);

    		insertAll();

    		return DB_SUCCESS;
    	}
	}

	/**
	 * 查询最新30条记录。
	 *
	 * records是TreeMap类型(BaseDao ->readDbFile),它把每次新增的记录都放在map的末尾处,
	 *
	 * 这样是为了能够模拟数据库的表.
	 *
	 * 所以为了实现查询最新的30条数据,要获得降序的map,之后取前30条
	 *
	 * new TreeMap().descendingMap(); new TreeMap().descendingKey();
	 *
	 */
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
    				list.add(new BrushCupBoardLog(JSON.toJSONString(treeMap.get(key))));
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