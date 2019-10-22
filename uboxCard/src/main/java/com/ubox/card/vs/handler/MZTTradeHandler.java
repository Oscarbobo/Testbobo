package com.ubox.card.vs.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ubox.card.db.dao.MZTTradeDao;
import com.ubox.card.util.logger.Logger;
import com.ubox.card.vs.VsConst;

public final class MZTTradeHandler implements Handler{

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> readLoacalData(Map<String, Object> requestData) {
		Object contentsData = requestData.get(VsConst.MAP_KEY_CONTENTS);
		if (!(contentsData instanceof Map<?, ?>)) {
			contentsData = new TreeMap<String, Object>();
		}
		// 将brushLog.db中的最新30条数据同步到服务器
		List<Object> mztTradeList = MZTTradeDao.getInstance().queryLatestMaxN(30);
		if (mztTradeList != null && mztTradeList.size() != 0) {
			((Map) contentsData).put(VsConst.MAP_KEY_MZT_TRADE, mztTradeList);
		} else {
			((Map) contentsData).put(VsConst.MAP_KEY_MZT_TRADE, new ArrayList<Object>());
		}

		requestData.put(VsConst.MAP_KEY_CONTENTS, contentsData);

		return requestData;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void deleteSynSuccessData(Map<String, Object> responseData) {
		Object contentsData = responseData.get(VsConst.MAP_KEY_CONTENTS);
		if (contentsData instanceof Map) {
			Object mztTradeIdList = ((Map)contentsData).get(VsConst.MAP_KEY_MZT_TRADE_SUCCESS);
			if (mztTradeIdList instanceof List && ((List) mztTradeIdList).size() > 0) {
				List<String> ids = new ArrayList<String>(((List) mztTradeIdList).size());
				for (Object o : (List) mztTradeIdList) {
					ids.add(String.valueOf(o));
				}
				MZTTradeDao.getInstance().deleteMany(ids);
				MZTTradeDao.getInstance().init(); // BaseDao.records和db文件内容同步
			}
		} else {
			Logger.warn("contentsData is not Map.Do not delete local data");
		}
	}
}

