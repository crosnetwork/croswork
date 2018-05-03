package com.cros.block.util;

import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;


/**
 * @ClassName: TripleDES
 * @Package org.weibei.blockchain.util
 * @Description:TODO 加密解密
 * @date: 2016年11月21日 上午8:52:34
 * @author hokuny@foxmail.com
 * @version 
 */
public class TripleDES {
	static {
		Security.addProvider(new com.sun.crypto.provider.SunJCE());
	}

	private static final String MCRYPT_TRIPLEDES = "DESede";
	private static final String TRANSFORMATION = "DESede/CBC/PKCS5Padding";

	public static byte[] decrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
		DESedeKeySpec spec = new DESedeKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(MCRYPT_TRIPLEDES);
		SecretKey sec = keyFactory.generateSecret(spec);
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		IvParameterSpec IvParameters = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, sec, IvParameters);
		return cipher.doFinal(data);
	}

	public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
		DESedeKeySpec spec = new DESedeKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
		SecretKey sec = keyFactory.generateSecret(spec);
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		IvParameterSpec IvParameters = new IvParameterSpec(iv);
		cipher.init(Cipher.ENCRYPT_MODE, sec, IvParameters);
		return cipher.doFinal(data);
	}

	public static byte[] encrypt(byte[] data) throws Exception {
		final byte[] secretBytes = new byte[] { 0x61, 0x75, 0x74, 0x6F, 0x68, 0x6F, 0x6D, 0x65, 0x63, 0x6F, 0x6F, 0x70, 0x65,
				0x72, 0x61, 0x74, 0x65, 0x6F, 0x70, 0x65, 0x6E, 0x61, 0x70, 0x69 };

		final byte[] ivbytes = new byte[] { 0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF };
		return encrypt(data, secretBytes, ivbytes);
	}

	public static byte[] generateSecretKey() throws NoSuchAlgorithmException {
		KeyGenerator keygen = KeyGenerator.getInstance(MCRYPT_TRIPLEDES);
		return keygen.generateKey().getEncoded();
	}

	public static byte[] randomIVBytes() {
		Random ran = new Random();
		byte[] bytes = new byte[8];
		for (int i = 0; i < bytes.length; ++i) {
			bytes[i] = (byte) ran.nextInt(Byte.MAX_VALUE + 1);
		}
		return bytes;
	}
/*
	public static void main(String args[]) throws Exception {
		String plainText = "2030:20140418:b11c523b846cd9bb7c283add4b6f2b5f";
		// final byte[] secretBytes = TripleDES.generateSecretKey();
		// final byte[] ivbytes = TripleDES.randomIVBytes();

		final byte[] secretBytes = new byte[] { 0x61, 0x75, 0x74, 0x6F, 0x68, 0x6F, 0x6D, 0x65, 0x63, 0x6F, 0x6F, 0x70, 0x65,
				0x72, 0x61, 0x74, 0x65, 0x6F, 0x70, 0x65, 0x6E, 0x61, 0x70, 0x69 };

		final byte[] ivbytes = new byte[] { 0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF };

		System.out.println("plain text: " + plainText);
		byte[] encrypt = TripleDES.encrypt(plainText.getBytes(), secretBytes, ivbytes);
		System.out.println("cipher text: " + encrypt);
		System.out.println("decrypt text: " + new String(TripleDES.decrypt(encrypt, secretBytes, ivbytes), "UTF-8"));
	}
	*/

}
