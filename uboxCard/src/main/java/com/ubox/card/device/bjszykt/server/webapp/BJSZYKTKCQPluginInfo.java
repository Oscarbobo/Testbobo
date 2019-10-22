/**
 * 
 */
package com.ubox.card.device.bjszykt.server.webapp;


/**
 * 
 * @author gaolei
 * @version 2015年9月23日
 * 
 */
public class BJSZYKTKCQPluginInfo implements WebServerPluginInfo{

	@Override
	public String[] getIndexFilesForMimeType(String mime) {
		// TODO Auto-generated method stub
		String[] indexFiles = {"index."+mime};
		return indexFiles;
	}

	@Override
	public String[] getMimeTypes() {
		// TODO Auto-generated method stub
		String[] mimes = {"ub"}; 
		return mimes;
	}

	@Override
	public WebServerPlugin getWebServerPlugin(String mimeType) {
		// TODO Auto-generated method stub
		return new BJSZYKTKCQPlugin();
	}

}
