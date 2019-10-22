package com.ubox.card.vs.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.os.Handler;

import com.ubox.card.db.dao.YNYLSFTradeDao;
import com.ubox.card.util.logger.Logger;
import com.ubox.card.vs.VsConst;

public class YNYLSFTradeHandler extends Handler implements
		com.ubox.card.vs.handler.Handler {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> readLoacalData(Map<String, Object> requestData) {
		Object contentsData = requestData.get(VsConst.MAP_KEY_CONTENTS);

		if (!(contentsData instanceof Map<?, ?>)) {
			contentsData = new TreeMap<String, Object>();
		}
		// 将brushLog.db中的最新30条数据同步到服务器
		int maxN = 30;

		List<Object> unionpayList = YNYLSFTradeDao.getInstance().queryLatestMaxN(maxN);
		if (unionpayList != null && unionpayList.size() != 0) {
			((Map) contentsData).put(VsConst.MAP_KEY_YNYLSF_TRADE, unionpayList);
		} else {
			// db中为空,传空值给服务器
			((Map) contentsData).put(VsConst.MAP_KEY_YNYLSF_TRADE, new ArrayList<Object>());
		}

		requestData.put(VsConst.MAP_KEY_CONTENTS, contentsData);

		return requestData;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void deleteSynSuccessData(Map<String, Object> responseData) {
		Object contentsData = responseData.get(VsConst.MAP_KEY_CONTENTS);

        if (contentsData instanceof Map) {
            Object unionpaySeqList = ((Map) contentsData).get(VsConst.MAP_KEY_YNYLSF_TRADE_SUCCESS);
            if (unionpaySeqList instanceof List && ((List) unionpaySeqList).size() > 0) {
                List<String> ids = new ArrayList<String>();
                for (Object o : (List) unionpaySeqList) {
                    String brushSeq = String.valueOf(o);
                    ids.add(brushSeq);
                }

                YNYLSFTradeDao.getInstance().deleteMany(ids);
                YNYLSFTradeDao.getInstance().init();
                Logger.info("YnylsfTradeDao init OK.");
            }
        } else {
            Logger.warn("contentsData is not Map.Do not delete local data");
        }
	}

}
