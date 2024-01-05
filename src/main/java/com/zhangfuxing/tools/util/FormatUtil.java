package com.zhangfuxing.tools.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/05/17
 * @email zhangfuxing1010@163.com
 */
public class FormatUtil {
	static final String PB = "PB";
	static final String GB = "GB";
	static final String MB = "MB";
	static final String KB = "KB";
	static final String Bit = "Bit";
	
	
	public static String formatFileSize(long fileSize, int scale) {
		BigDecimal c = new BigDecimal("1024.0");
		BigDecimal size = new BigDecimal(fileSize);
		String unit = getFileSizeUnit(fileSize);
		BigDecimal kb = c.multiply(c);
		final BigDecimal mb = kb.multiply(c);
		BigDecimal gb = mb.multiply(c);
		return switch (unit) {
			case Bit -> size + Bit;
			case KB -> size.divide(c, scale,RoundingMode.UP).floatValue() + KB;
			case MB -> size.divide(kb, scale, RoundingMode.UP).floatValue() + MB;
			case GB -> size.divide(mb, scale, RoundingMode.UP).floatValue() + GB;
			default -> size.divide(gb, scale, RoundingMode.UP).doubleValue() + PB;
		};
	}
	
	public static String formatFileSize(Long fileSize) {
		return formatFileSize(fileSize, 1);
	}
	
	public static String formatFileSize(long fileSize) {
		return formatFileSize(fileSize, 1);
	}
	
	private static String getFileSizeUnit(long fileSize) {
		long basic = 1024;
		if (fileSize < basic) return Bit;
		
		if (fileSize < (basic * basic)) return KB;
		
		if (fileSize < (basic * basic * basic)) return MB;
		
		if (fileSize < (basic * basic * basic * basic)) return GB;
		return PB;
	}
}
