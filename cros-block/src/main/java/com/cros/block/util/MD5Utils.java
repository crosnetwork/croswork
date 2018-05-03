package com.cros.block.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Locale;

public class MD5Utils {

	/**
	 * 密码加密
	 * @param inStr
	 * @return
	 * @throws Exception
	 */
	public static String MD5Encrypt(String inStr) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5"); // 可以选中其他的算法如SHA
		byte[] digest = md.digest(inStr.getBytes("GBK")); // 返回的是byet[]，要转化为String存储比较方便
		String outStr = byteToString(digest);
		return outStr;
	}

	/**
	 * byteToString
	 * @param digest
	 * @return
	 * @throws Exception
	 */
	private static String byteToString(byte[] digest) throws Exception {
		String str = "";
		String tempStr = "";
		StringBuffer sb = new StringBuffer("");
		for (int i = 1; i < digest.length; i++) {
			tempStr = (Integer.toHexString(digest[i] & 0xff));
			if (tempStr.length() == 1) {
				sb.append("0");
				sb.append(tempStr);
			} else {
				sb.append(tempStr);
			}
		}
		str = sb.toString().toUpperCase(Locale.US);
		return str;
	}
	
	//int 类型简单加密
	public static String setInt(int a) throws Exception {
		String[] str = {"a","b","c","d","e","f","g","h","i","j","k","0","1","2","3","4","5","6","7","8","9"};
    	String str1 = str[(int)(0+Math.random()*20)]+str[(int)(0+Math.random()*20)]+str[(int)((int)(0+Math.random()*20))]
				+Integer.toHexString(a)
				+str[(int)((int)(0+Math.random()*20))]+str[(int)((int)(0+Math.random()*20))]+str[(int)((int)(0+Math.random()*20))];
		return str1;
	}
	
	public static int getInt(String str) throws Exception {
		String s = str.substring(3, str.length()-3);
		return Integer.parseInt(s,16);
	}
	/*
	 *  传文件路径
	 */
	public static byte[] createChecksum(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		// "MD5"，"SHA1"，"SHA-256"，"SHA-384"，"SHA-512"
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return complete.digest();
	}
	/*
	 * 文件创建文件hash值  需要
	 */
	public static String getMD5Checksum(String path) throws Exception {
		byte[] b = createChecksum(path);
		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public static String getMD5ChecksumForInputStream(InputStream filename) throws Exception {
		byte[] b = createChecksumForInputStream(filename);
		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	/*
	 *  传文件流
	 */
	public static byte[] createChecksumForInputStream(InputStream filename) throws Exception {

		byte[] buffer = new byte[1024];
		// "MD5"，"SHA1"，"SHA-256"，"SHA-384"，"SHA-512"
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = filename.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

//		filename.close();
		return complete.digest();
	}

	public final static String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			byte[] btInput = s.getBytes("utf-8");
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
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
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
