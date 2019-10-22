package com.ubox.card.deploy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.ubox.card.util.logger.Logger;
import com.ubox.util.UboxConfigTool;

public class Writer {
	
	/**
	 * 写入信息到card.json里面中
	 * 
	 * @param cardJson card.json信息
	 */
	public void writeCardJson(CardJson cardJson) {
		try {
			String CONFIG_PATH 	    = UboxConfigTool.getUboxDir().getAbsolutePath() + "/Config/";
			String CONFIG_FILE_NAME = "card.json";
			
			String         json = com.alibaba.fastjson.JSON.toJSONString(cardJson, true);
			File           file = holdFileRef(CONFIG_PATH, CONFIG_FILE_NAME);
			BufferedWriter br   = new BufferedWriter(new FileWriter(file));
			
			br.write(json);br.close();
		} catch (Exception e) {
			Logger.error("Write card.json error", e);
		}
	}
	
    /**
     * 获取指定路径下的文件句柄.如果文件不存在,则创建路径和文件
     *
     * @param path 指定路径
     * @param fileName 指定文件
     * @return 文件句柄
     */
    private File holdFileRef(String path, String fileName) {
        File p = new File(path);
        if(!p.exists())  Logger.info((p.mkdirs() ? ">>>>SUCCESS: " : ">>>>FAIL: ") + "Create path=" + path);

        File f = new File(path + File.separator + fileName);
        if(!f.exists()) try {
            Logger.info((f.createNewFile() ? ">>>>SUCCESS: " : ">>>>FAIL: ") +  "Create fileName=" + fileName);
        } catch (IOException e) {
            Logger.error(">>>>FAIL: Create file fail, file name=" + fileName, e);
        }

        return f;
    }
}
