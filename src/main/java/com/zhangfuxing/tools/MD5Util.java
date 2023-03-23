package com.zhangfuxing.tools;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zhangfx
 * @date 2023/3/16
 */
public class MD5Util {
	public static final Charset defauleCharset = StandardCharsets.UTF_8;
	
	public static String encode(String source, Charset charset, Boolean isUpperCase) {
		String md5;
		try {
			md5 = StringUtil.toMD5(source, charset, isUpperCase);
		} catch (NoSuchAlgorithmException e) {
			md5 = "";
		}
		return md5;
	}
	
	public static String encode(String source, Boolean isUpperCase) {
		return encode(source, defauleCharset, isUpperCase);
	}
	
	public static String encode(String source, Charset charset) {
		return encode(source, charset, true);
	}
	
	public static String encode(String source) {
		return encode(source, defauleCharset, true);
	}
	
	public static String encode(File file, Boolean isUpperCase, int size) throws IOException {
		if (file == null || !file.exists()) return "";
		InputStream is = new FileInputStream(file);
		return encode(is, isUpperCase, size);
	}
	
	public static String encode(File file, int size) throws IOException {
		return encode(file, true, size);
	}
	
	public static String encode(File file, Boolean isUpperCase) throws IOException {
		return encode(file, isUpperCase, 0);
	}
	
	public static String encode(File file) throws IOException {
		return encode(file, true, 0);
	}
	
	public static String encode(InputStream inputStream, Boolean isUpperCase, int size) throws IOException {
		if (inputStream == null) return "";
		try (inputStream) {
			byte[] buf;
			if (size <= 0) buf = new byte[1 << 12];
			else buf = new byte[size];
			int len = inputStream.read(buf);
			char[] chars = StringUtil.encode(getMd5Bytes(buf), isUpperCase);
			return String.valueOf(chars);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String encode(InputStream inputStream, Boolean isUpperCase) throws IOException {
		return encode(inputStream, isUpperCase, 0);
	}
	
	public static String encode(InputStream inputStream, int size) throws IOException {
		return encode(inputStream, true, size);
	}
	
	public static String encode(InputStream inputStream) throws IOException {
		return encode(inputStream, true, 0);
	}
	
	public static byte[] getMd5Bytes(byte[] bytes) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		return digest.digest(bytes);
	}
}
