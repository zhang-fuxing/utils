package com.zhangfuxing.tools.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/18
 * @email zhangfuxing1010@163.com
 */
public abstract class JsonBase {
	protected final ObjectMapper objectMapper;

	protected JsonBase() {
		this.objectMapper = createConfiguredMapper();
	}

	protected static ObjectMapper createConfiguredMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	protected static boolean validateResource(Object obj) {
		// 统一校验逻辑
		if (obj instanceof File f) {
			return f.exists() && !f.isDirectory();
		}
		if (obj instanceof URL url) {
			return isAccessible(url);
		}
		if (obj instanceof byte[] bytes) {
			return bytes.length > 0;
		}
		return true;
	}

	protected static boolean isAccessible(URL url) {
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isJson(String str) {
		if (!validateBasicStructure(str)) return false;

		try {
			createConfiguredMapper().readTree(str);
			return true;
		} catch (JsonProcessingException e) {
			return false;
		}
	}

	public static boolean isJsonArray(String str) {
		if (!validateBasicStructure(str)) return false;

		try {
			createConfiguredMapper().readTree(str);
			return true;
		} catch (JsonProcessingException e) {
			return false;
		}
	}

	public static boolean isJsonObject(String str) {
		if (!validateBasicStructure(str)) return false;

		try {
			createConfiguredMapper().readTree(str);
			return true;
		} catch (JsonProcessingException e) {
			return false;
		}
	}

	private static boolean validateBasicStructure(String str) {
		return validateArrayStructure(str) || validateObjectStructure(str);
	}

	private static boolean validateArrayStructure(String str) {
		if (str == null || str.isBlank()) return false;

		String trimmed = str.trim();
		if (trimmed.length() < 2) return false;

		char first = trimmed.charAt(0);
		char last = trimmed.charAt(trimmed.length() - 1);
		return (first == '[' && last == ']');
	}

	private static boolean validateObjectStructure(String str) {
		if (str == null || str.isBlank()) return false;

		String trimmed = str.trim();
		if (trimmed.length() < 2) return false;

		char first = trimmed.charAt(0);
		char last = trimmed.charAt(trimmed.length() - 1);
		return (first == '{' && last == '}');
	}
}
