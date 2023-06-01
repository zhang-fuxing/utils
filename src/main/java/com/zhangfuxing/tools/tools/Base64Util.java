package com.zhangfuxing.tools.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/05/06
 * @email zhangfuxing1010@163.com
 */
public class Base64Util {
	public static String encode(String str) {
		return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
	}
	
	public static String encode(InputStream inputStream) {
		try (inputStream) {
			byte[] bytes = inputStream.readAllBytes();
			return Base64.getEncoder().encodeToString(bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String imageEncode(InputStream inputStream, String type) {
		String data = encode(inputStream);
		String img = "data:image/%s;base64,%s";
		String f = ".";
		if (type.contains(f)) {
			type = type.substring(type.lastIndexOf(f) + 1);
		}
		if ("ico".equals(type) || "icon".equals(type)) {
			type = "x-icon";
		}
		
		return img.formatted(type, data);
	}
	
	public static String imageEncode(File file) throws FileNotFoundException {
		FileInputStream is = new FileInputStream(file);
		return imageEncode(is, file.getName());
	}
}
