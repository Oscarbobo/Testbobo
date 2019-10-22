package com.ubox.card.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FileUtil {
	/*
	 * 文件中内容按行读入到内存列表
	 */
	public synchronized static Map<String,Object> fileToMap(String pathName,String fileName) throws Exception{
		Map<String,Object> map = new HashMap<String, Object>();
		
		InputStream is = null;
		BufferedReader reader = null;
		try {
			File filePath = new File(pathName);
			if(!filePath.exists()){
				filePath.mkdirs();
			}
			
			File file = new File(pathName + fileName);
			if(!file.exists()){
				file.createNewFile();
			}
			
			is = new FileInputStream(file);

			String line; // 用来保存每行读取的内容
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((line = reader.readLine()) != null) { // 如果 line 为空说明读完了
				if(!"".equals(line)){
					map.put(line, "");
				}
			}

			return map;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (is != null)
					is.close();
				if (reader != null)
					reader.close();
			} catch (Exception e) {
				throw e;
			}
		}
	}

	/**
	 * 字符串按行写到文件
	 * @param fileName 文件名
	 * @param data 字符串数据
	 * @throws IOException 
	 */
	public synchronized static void write(String fileName,String data,boolean append) throws Exception{
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			String path = fileName.substring(0, fileName.lastIndexOf("/"));
			File newDir = new File(path);
			if (!newDir.exists()) {// 检查父目录是否存在，不存在就创建
				newDir.mkdirs();
			}
			fw = new FileWriter(fileName,append);
			bw = new BufferedWriter(fw);
			bw.write(new String(data.getBytes(),"UTF-8"));
			bw.newLine();

			bw.flush();
			fw.flush();
		} catch (Exception ex) {
			throw ex;
		} finally {
			try {
				if (null != bw){
					bw.close();
				}
				if(null != fw){
					fw.close();
				}
			} catch (Exception e) {
				throw e;
			}
		}
	}
	
	/**
	 * map数据按行写到文件，key+sperator+value
	 * @param fileName
	 * @param map
	 * @param sperator
	 * @param append
	 * @throws Exception
	 */
	public static void mapToFile(String fileName,Map<String,String> map,String sperator,boolean append) throws Exception{
		try {
			Iterator<String> it = map.keySet().iterator();
			StringBuffer sb = new StringBuffer("");
			while(it.hasNext()){
				String key = it.next();
				String value = map.get(key);
				sb.append(key+sperator+value);
				sb.append("\n");
			}
			write(fileName, sb.toString().trim(), append);
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	public synchronized static List<String> readFile(String filePath, String sperator) {
		BufferedReader reader = null;
		List<String> list = new ArrayList<String>();
		try {
			if (org.apache.commons.lang.StringUtils.isEmpty(filePath))
				return null;
			File tempFile = new File(filePath);
			if (!(tempFile.exists() && tempFile.length() > 0)) {
				return null;
			}

			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(tempFile), "gb2312"));
			String line = null;
			if ((line = reader.readLine()) != null) {
				if (line.contains(sperator)) {
					String[] str = line.split(sperator);
					for (int i = 0, j = str.length; i < j; i++) {
						list.add(str[i]);
					}
				}
			}
		} catch (Exception e) {
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception e) {
			}
		}
		return list;
	}
	
}
