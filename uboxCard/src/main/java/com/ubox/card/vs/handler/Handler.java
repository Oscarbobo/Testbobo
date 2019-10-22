package com.ubox.card.vs.handler;

import java.util.Map;

public interface Handler {

	/**
	 * 读取本地db数据
	 * 
	 * @param requestData
	 * @return
	 */
	Map<String, Object> readLoacalData(Map<String, Object> requestData);

	/**
	 * 删除同步成功的数据,并且同步内存与实体文件
	 * 
	 * @param responseData
	 */
	void deleteSynSuccessData(Map<String, Object> responseData);

}
