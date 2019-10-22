package com.ubox.card.vs.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ubox.card.db.dao.BrushCupBoardLogDao;
import com.ubox.card.util.logger.Logger;
import com.ubox.card.vs.VsConst;

public class BrushCupBoardLogHandler implements Handler {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> readLoacalData(Map<String, Object> requestData) {
		Object contentsData = requestData.get(VsConst.MAP_KEY_CONTENTS);

		if (!(contentsData instanceof Map<?, ?>)) {
			contentsData = new TreeMap<String, Object>();
		}
		// 将brushLog.db中的最新30条数据同步到服务器
		int maxN = 30;

		List<Object> brushLogList = BrushCupBoardLogDao.getInstance().queryLatestMaxN(maxN);
		if (brushLogList != null && brushLogList.size() != 0) {
			((Map) contentsData).put(VsConst.MAP_KEY_BRUSHCUPBOARDLOG, brushLogList);
		} else {
			// db中为空,传空值给服务器
			((Map) contentsData).put(VsConst.MAP_KEY_BRUSHCUPBOARDLOG, new ArrayList<Object>());
		}

		requestData.put(VsConst.MAP_KEY_CONTENTS, contentsData);

		return requestData;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void deleteSynSuccessData(Map<String, Object> responseData) {
		Object contentsData = responseData.get(VsConst.MAP_KEY_CONTENTS);
		if (contentsData instanceof Map) {
			Object brushSeqList = ((Map)contentsData).get(VsConst.MAP_KEY_BRUSHCUPBOARDLOG_SUCCESS);
			if (brushSeqList instanceof List && ((List) brushSeqList).size() > 0) {
				List<String> ids = new ArrayList<String>(((List) brushSeqList).size());
				
				for (Object o : (List) brushSeqList) {
					String brushSeq = String.valueOf(o);
					ids.add(brushSeq);
				}
				
				BrushCupBoardLogDao.getInstance().deleteMany(ids);
				BrushCupBoardLogDao.getInstance().init();
			}
		} else {
			Logger.warn(">>>> contentsData is not Map.Do not delete local data");
		}
	}
}
