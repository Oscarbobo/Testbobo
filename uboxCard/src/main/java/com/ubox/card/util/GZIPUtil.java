package com.ubox.card.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;


/**
 * @author huyuguo
 * @description 压缩组件
 * @fileName com.ubox.utils.zip.GZIP.java
 * @date 2011-9-6 上午10:49:47
 */
public class GZIPUtil {
	
	private static final int BUFFER = 1024;
    public static final String EXT = ".gz";
    
    /**
     * 数据压缩
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] compress(byte[] data) throws Exception{
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        // 压缩
        compress(in, out);
        
        byte[] output = out.toByteArray();
        
        out.flush();
        out.close();
        
        in.close();
        return output;
    }
    
    /**
     * 文件压缩并删除原始文件
     * @param file
     * @throws Exception
     */
    public static void compress(File file) throws Exception {
        compress(file, true);
    }
    
    /**
     * 文件 压缩
     * @param file
     * @param delete    是否删除原始文件
     * @throws Exception
     */
    public static void compress(File file, boolean delete) throws Exception {
        if(!file.exists()) {
            return;
        }
        FileInputStream in = new FileInputStream(file);
        FileOutputStream out = new FileOutputStream(file.getPath() + EXT);
        compress(in, out);
        in.close();
        out.flush();
        out.close();
        if(delete) {
            file.delete();
        }
    }
    
    /**
     * 数据压缩
     * @param in
     * @param out
     * @throws Exception
     */
    public static void compress(InputStream in, OutputStream out) 
            throws Exception{
        GZIPOutputStream gos = new GZIPOutputStream(out);
        
        int count;
        byte data[] = new byte[BUFFER];
        while((count = in.read(data, 0, BUFFER)) != -1) {
            gos.write(data, 0, count);
        }
        gos.finish();
//        gos.flush();//android4.4.4在使用GZIPOutputStream进行压缩的时候报异常，此问题只需要调用gos.finish()就可以，close、flush删掉其中一个或两个（在深圳德卡工控上出现这个问题，在此注释flush，程序不报异常）
        gos.close();
    }
    
    /**
     * 文件压缩
     * @param path
     * @throws Exception
     */
    public static void compress(String path) throws Exception {
        compress(path, true);
    }
    
    /**
     * 文件压缩
     * @param path
     * @param delete
     * @throws Exception
     */
    public static void compress(String path, boolean delete) throws Exception {
        File file = new File(path);
        compress(file, delete);
    }
    
    /**
     * 数据解压缩
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] decompress(byte[] data) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        // 解压缩
        decompress(in, out);
        
        data = out.toByteArray();
        
        out.flush();
        out.close();
        
        in.close();
        
        return data;
    }
    
    /**
     * 文件解压缩并删除原始文件
     * @param file
     * @throws Exception
     */
    public static void decompress(File file) throws Exception {
        decompress(file, true);
    }
    
    /**
     * 文件解压缩
     * @param file
     * @param delete 删除原始文件
     * @throws Exception
     */
    public static void decompress(File file, boolean delete) throws Exception {
        if(!file.exists()) {
            return;
        }
        FileInputStream in = new FileInputStream(file);
        FileOutputStream out = new FileOutputStream(
                file.getPath().replace(EXT, ""));
        decompress(in, out);
        
        in.close();
        
        out.flush();
        out.close();
        if(delete) {
            file.delete();
        }
    }
    
    /**
     * 数据解压缩
     * @param in
     * @param out
     * @throws Exception
     */
    public static void decompress(InputStream in, OutputStream out)
            throws Exception {
        GZIPInputStream gis = new GZIPInputStream(in);
        
        int count;
        byte data[] = new byte[BUFFER];
        while((count = gis.read(data, 0, BUFFER)) != -1) {
            out.write(data, 0, count);
        }
        
        gis.close();
    }
    
    /**
     * 文件解压缩并删除原始文件
     * @param path
     * @throws Exception
     */
    public static void decompress(String path) throws Exception {
        decompress(path, true);
    }
    
    /**
     * 文件解压缩
     * @param path
     * @param delete
     * @throws Exception
     */
    public static void decompress(String path, boolean delete) 
            throws Exception {
        File file = new File(path);
        decompress(file, delete);
    }
    
    /**
     * 将文件压缩返回二进制数据
     * @param file
     * @return
     */
    public static byte[] gz(File file) {
        try{
            byte[] bytes = FileUtils.readFileToByteArray(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzout = new GZIPOutputStream(out);
            
            gzout.write(bytes);
            gzout.flush();
            gzout.close();
            byte[] ret = out.toByteArray();
            out.close();
            
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return new byte[0];
    }
}
