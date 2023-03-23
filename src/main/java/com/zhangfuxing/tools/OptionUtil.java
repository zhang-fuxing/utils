package com.zhangfuxing.tools;

import java.io.*;
import java.util.*;

public class OptionUtil {
	public static final String Separator1 = "/";
	private static final String passSource = "qwertyuiopasdfghjklzxcvbnm0123456789.[]QWERTYUIOPASDFGHJKLZXCVBNM!#=+-";
	
	public static String bytesToStr(byte[] bytes) {
		String str;
		StringBuilder stringBuilder = new StringBuilder();
		for (byte aByte : bytes) {
			int v = aByte & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		str = stringBuilder.toString();
		return str;
	}
	
	public static byte[] strToBytes(String str) {
		byte[] buf = new byte[20];
		for (int i = 0; i < str.length() / 2; i++) {
			String twstr = str.substring(i * 2, i * 2 + 2);//
			buf[i] = (byte) Integer.parseInt(twstr, 16);
		}
		return buf;
	}
	
	public static String getSqlserverDate(String Separator) {
		return OptionUtil.Separator1.equals(Separator) ?
				"CONVERT(varchar,dateadd(dd,-day(getdate())+1,getdate()),111)"
				: "CONVERT(varchar,dateadd(dd,-day(getdate())+1,getdate()),126)";
	}
	
	public static String getOracleDate(String Separator) {
		return OptionUtil.Separator1.equals(Separator) ?
				"TO_CHAR(TRUNC(SYSDATE, 'mm'),'yyyy/mm/dd')"
				: "TO_CHAR(TRUNC(SYSDATE, 'mm'),'yyyy-mm-dd')";
	}

	
	/**
	 * 生成随机字符串用作密钥
	 *
	 * @param passLen 字符串长度，如果该值为0，则默认生成长度为32字符串
	 * @return 指定长度字符串
	 */
	public static String generatorPassword(final int passLen) {
		Random random = new Random(System.currentTimeMillis());
		StringBuilder password = new StringBuilder();
		int len = passLen == 0 ? 32 : passLen;
		for (int i = 0; i < len; i++) {
			int index = random.nextInt(passSource.length());
			password.append(passSource.charAt(index));
		}
		return password.toString();
	}
	
	public static String generatorPassword() {
		return generatorPassword(0);
	}
	
	public static <T> Set<T> toSet(T[] arr) {
		if (arr == null) return new HashSet<>(0);
		Set<T> res = new HashSet<>(arr.length);
		Collections.addAll(res, arr);
		return res;
	}
	
	public static <T> List<T> toList(T[] arr) {
		if (arr == null) return new ArrayList<>(0);
		List<T> res = new ArrayList<>(arr.length);
		Collections.addAll(res, arr);
		return res;
	}
	
	public static boolean and(boolean... tar) {
		if (tar == null || tar.length == 0) return false;
		boolean result = true;
		for (boolean var : tar) {
			result = (result && var);
		}
		return result;
	}
	
	public static boolean or(boolean... b) {
		if (b == null || b.length == 0) return false;
		if (b.length == 1) return b[0];
		boolean result = b[0];
		for (int i = 1; i < b.length; i++) {
			result = (result || b[i]);
		}
		return result;
	}
	
	public static long allPage(long count, long pageSize) {
		long page = count / pageSize;
		long rd = count % pageSize;
		return rd == 0 ? page : page + 1;
	}
	
	public static void writeFile(String filePath, OutputStream outputStream) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) throw new FileNotFoundException("不存在文件： %s，请确保该文件存在。 ".formatted(filePath));
		writeFile(file, outputStream);
	}
	public static void writeFile(File file, OutputStream outputStream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		BufferedOutputStream bos = new BufferedOutputStream(outputStream);
		try (bis;bos) {
			byte[] bytes = new byte[1024 * 4];
			int len = -1;
			while ((len = bis.read(bytes)) != -1) {
				bos.write(bytes, 0, len);
			}
			bos.flush();
		}
	}
}






















