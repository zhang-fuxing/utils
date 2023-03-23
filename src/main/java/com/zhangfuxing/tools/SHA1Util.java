package com.zhangfuxing.tools;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zhangfx
 * @date 2023/3/23
 */
public class SHA1Util {
	static MessageDigest messageDigest;
	
	static {
		try {
			messageDigest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
	private static String toString(byte[] bytes) {
		int len = bytes.length;
		StringBuilder buf = new StringBuilder(len * 2);
		// 把密文转换成十六进制的字符串形式
		for (byte aByte : bytes) {
			buf.append(HEX_DIGITS[(aByte >> 4) & 0x0f]);
			buf.append(HEX_DIGITS[aByte & 0x0f]);
		}
		return buf.toString();
	}
	
	public static String encode(String str) {
		if (StringUtil.isEmpty(str)) {
			return null;
		}
		try {
			byte[] input = str.getBytes(StandardCharsets.UTF_8); //"utf8"
			messageDigest.update(input);
			byte[] output = messageDigest.digest();
			return toString(output);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String encode(String str, String charset) {
		if (StringUtil.isEmpty(str)) {
			return null;
		}
		try {
			byte[] input = str.getBytes(charset); //"utf8"
			messageDigest.update(input);
			byte[] output = messageDigest.digest();
			return toString(output);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
