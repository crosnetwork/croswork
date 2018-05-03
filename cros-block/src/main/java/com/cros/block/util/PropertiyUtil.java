package com.cros.block.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * @ClassName: PropertiyUtil
 * @Package org.weibei.blockchain.util
 * @Description:TODO ADD FUNCTION
 * @date: 2016年11月21日 上午8:52:16
 * @author hokuny@foxmail.com
 * @version 
 */
public class PropertiyUtil {
	private Properties prop;
	public PropertiyUtil(String fileName){
		prop = new Properties();
		InputStream in = getClass().getResourceAsStream(fileName);
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getString(String key){
		return prop.getProperty(key);
	}
	
	public int getInt(String key){
		String value = prop.getProperty(key);
		return Integer.parseInt(value);
	}
	
	public long getLong(String key){
		String value = prop.getProperty(key);
		return Long.parseLong(value);
	}
	
	public double getDouble(String key){
		String value = prop.getProperty(key);
		return Double.parseDouble(value);
	}
	public static void main(String[] args) {
		PropertiyUtil propUtil = new PropertiyUtil("/config.properties");
		String host = propUtil.getString("host");
		System.out.println(host);
	}
}
