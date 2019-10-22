package com.ubox.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

public class UboxTool {

	public static JSONArray getGZIP(String dataStr) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JSONArray outArray = new JSONArray();
		try {

			GZIPOutputStream gzip = new GZIPOutputStream(out);
			gzip.write(dataStr.getBytes());
			gzip.close();
			byte[] outByte = out.toByteArray();
			for (int i = 0; i < outByte.length; i++) {
				outArray.put(outByte[i]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return outArray;
	}


	public static JSONObject getSpInfo(JSONObject json) {
		JSONObject spJson = new JSONObject();

		try {
			JSONObject data = json.getJSONObject("datas");
			JSONArray spList = (JSONArray) data.get("spList");
			if (spList != null && spList.length() > 0) {
				for (int i = 0; i < spList.length(); i++) {
					JSONObject spData = spList.getJSONObject(i);
					spJson.put(spData.getInt("id") + "", spData);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return spJson;
	}

	/**
	 * 判断当前时间是否在开始时间和结束时间之内 不包括等于
	 * 
	 * @param begin
	 *            "yyyy-MM-dd HH:mm:ss"
	 * @param end
	 *            "yyyy-MM-dd HH:mm:ss"
	 * @return
	 */
	public static boolean IsNowValid(String beginStr, String endStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Calendar currentTime = Calendar.getInstance();

			Calendar begin = Calendar.getInstance();
			begin.setTimeInMillis(dateFormat.parse(beginStr).getTime());

			Calendar end = Calendar.getInstance();
			end.setTimeInMillis(dateFormat.parse(endStr).getTime());

			if (currentTime.after(begin) && currentTime.before(end)) {
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 * @param time
	 *            "yyyy-MM-dd HH:mm:ss"
	 * @return
	 */
	public static boolean IsAfterNow(String timeStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Calendar currentTime = Calendar.getInstance();

			Calendar time = Calendar.getInstance();
			time.setTimeInMillis(dateFormat.parse(timeStr).getTime());

			return currentTime.after(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String unZip(String data) {

		String reData = "";
		try {
			JSONArray s = new JSONArray(data);
			byte strByte[] = new byte[s.length()];
			for (int i = 0; i < s.length(); i++) {
				Integer d = s.getInt(i);
				strByte[i] = d.byteValue();
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayInputStream in = new ByteArrayInputStream(strByte);
			GZIPInputStream gunzip = new GZIPInputStream(in);
			byte[] buffer = new byte[256];
			int n;
			while ((n = gunzip.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
			reData = out.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reData;

	}

	/**
	 * 解压指定zip文件
	 * 
	 * @param zipfile
	 *            压缩文件的路径
	 * @param destFile
	 *            　　　解压到的目录　
	 */
	public static void unZipFile(String zipfile, String destFileDir) {

		byte[] buf = new byte[512];
		int readedBytes;
		try {
			FileOutputStream fileOut;
			File file;
			InputStream inputStream;
			ZipFile zipFile = new ZipFile(zipfile);

			for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				file = new File(destFileDir + File.separator + entry.getName());
				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					// 如果指定文件的目录不存在,则创建之
					File parent = file.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					inputStream = zipFile.getInputStream(entry);
					fileOut = new FileOutputStream(file);
					while ((readedBytes = inputStream.read(buf)) > 0) {
						fileOut.write(buf, 0, readedBytes);
					}
					fileOut.close();
					inputStream.close();
				}
			}
			zipFile.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * 获取md5值
	 * 
	 * @return md5值
	 */
	public static String getMd5(String oldStr) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {

			// 获得MD5摘要算法 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.reset();
			// 使用指定的字节更新摘
			mdInst.update(oldStr.getBytes());
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str).toLowerCase();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取文件md5值
	 * 
	 * @param filePath
	 * @return md5值
	 */
	public static String getFileMd5(File file) {

		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		byte[] md = digest.digest();

		int j = md.length;
		char str[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			byte byte0 = md[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		String ddd = new String(str).toLowerCase();

		return ddd;
	}


	/**
	 * 比对文件md5值
	 * 
	 * @param file
	 * @param md5
	 */
	public static boolean compareFileMd5(File file, String md5) {

		if (TextUtils.isEmpty(md5)) {
			return true;
		}

		if (file.exists()) {
			String fileMd5 = getFileMd5(file);
			if (md5.equals(fileMd5)) {
				return true;
			}
		}
		return false;
	}

	public static void writeFile(String path, String content) {

		try {
			FileWriter fw = new FileWriter(path, true);
			String c = content + "\r\n";
			fw.write(c);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 读取数据
	 * 
	 * @return
	 */
	public static String readFile(String path) {

		String content = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			content = br.readLine();
			br.close();
		} catch (IOException e) {
		}
		return content;
	}

	@SuppressLint("NewApi")
	public static String getLocalMacAddressFromIp(Context context) {

		String mac_s = "";
		try {
			byte[] mac;
			NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(getLocalIpAddress()));
			mac = ne.getHardwareAddress();
			mac_s = byte2hex(mac);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mac_s;
	}

	public static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer(b.length);
		String stmp = "";
		int len = b.length;
		for (int n = 0; n < len; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1)
				hs = hs.append("0").append(stmp);
			else {
				hs = hs.append(stmp);
			}
		}
		return String.valueOf(hs);
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isAnyLocalAddress() && !inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress()) {

						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
		}

		return null;
	}

	public static void base64ToPic(String base64Str, String path, String fileName) {

		if (TextUtils.isEmpty(base64Str)) {
			return;
		}

		File dir = new File(path);

		if (!dir.exists()) {
			dir.mkdirs();
		}

		byte picBuff[] = android.util.Base64.decode(base64Str, 0);
		File headPic = new File(path + fileName);
		try {
			FileOutputStream os = new FileOutputStream(headPic);
			os.write(picBuff, 0, picBuff.length);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void copyFile(File sourcefile, File targetFile) {

		try {
			// 新建文件输入流并对它进行缓冲
			FileInputStream input = new FileInputStream(sourcefile);
			BufferedInputStream inbuff = new BufferedInputStream(input);

			// 新建文件输出流并对它进行缓冲
			FileOutputStream out = new FileOutputStream(targetFile);
			BufferedOutputStream outbuff = new BufferedOutputStream(out);

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len = 0;
			while ((len = inbuff.read(b)) != -1) {
				outbuff.write(b, 0, len);
			}

			// 刷新此缓冲的输出流
			outbuff.flush();

			// 关闭流
			inbuff.close();
			outbuff.close();
			out.close();
			input.close();
		} catch (IOException e) {
			
		}

	}

	public static void copyDirectiory(String sourceDir, String targetDir) {

		// 新建目标目录

		File sorce = new File(sourceDir);
		if (!sorce.exists()) {
			return;
		}

		File target = new File(targetDir);

		if (!target.exists()) {
			target.mkdirs();
		}

		// 获取源文件夹当下的文件或目录
		File[] file = (new File(sourceDir)).listFiles();
		if(file == null) return;
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());
				copyFile(sourceFile, targetFile);
			}

			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + "/" + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();
				copyDirectiory(dir1, dir2);
			}
		}

	}

	public static boolean deleteDirectory(String sPath) {
		boolean flag = false;
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		if(files == null) return true;
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * sha1加密
	 * 
	 * @param s
	 * @return
	 */
	public static String getSHA1(String s) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			return toHexString(messageDigest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String toHexString(byte[] keyData) {
		if (keyData == null) {
			return null;
		}
		int expectedStringLen = keyData.length * 2;
		StringBuilder sb = new StringBuilder(expectedStringLen);
		for (int i = 0; i < keyData.length; i++) {
			String hexStr = Integer.toString(keyData[i] & 0x00FF, 16);
			if (hexStr.length() == 1) {
				hexStr = "0" + hexStr;
			}
			sb.append(hexStr);
		}
		return sb.toString();
	}


	public static String guessAppropriateEncoding(CharSequence contents) {
		for (int i = 0; i < contents.length(); i++) {
			if (contents.charAt(i) > 0xFF) {
				return "UTF-8";
			}
		}
		return null;
	}

	public static void saveMyBitmap(String saveDir, String saveName, Bitmap mBitmap) {

		File fileDir = new File(saveDir);

		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}

		File file = new File(saveDir + saveName);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(file);
			mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
