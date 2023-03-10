package org.tools.option;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangfx
 * @date 2023/3/10
 */
public class Str_Util {
	public static final int INDEX_NOT_FOUND = -1;
	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
	
	public static boolean notEmpty(String s) {
		return !isEmpty(s);
	}
	
	public static String requireNonNullElseGet(String pattern, String s) {
		return notEmpty(pattern) ? pattern : s;
	}
	
	public static boolean fieldCheck(String str) {
		String pattern = "^((?!['/*\\-]).)*$";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(str);
		return m.matches();
	}
	
	public static boolean isBlank(final CharSequence cs) {
		final int strLen = length(cs);
		if (strLen == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static int length(final CharSequence cs) {
		return cs == null ? 0 : cs.length();
	}
	
	public static String remove(final String str, final char remove) {
		if (isEmpty(str) || str.indexOf(remove) == INDEX_NOT_FOUND) {
			return str;
		}
		final char[] chars = str.toCharArray();
		int pos = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] != remove) {
				chars[pos++] = chars[i];
			}
		}
		return new String(chars, 0, pos);
	}

}
