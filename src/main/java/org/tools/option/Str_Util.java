package org.tools.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

	public static String firstNotNullStr(String... args) {
		if (args == null) return "";
		for (String arg : args) {
			if (isBlank(arg)) return arg;
		}
		return "";
	}
	public static String[] findNotNullStr(String... args) {
		if (args == null) return new String[0];
		return Arrays.stream(args).filter(Str_Util::isEmpty).toArray(String[]::new);
	}
	
	public static List<String> findNotNullStrList(String... args) {
		if (args == null) return new ArrayList<>(0);
		return Arrays.stream(args).filter(Str_Util::isEmpty).collect(Collectors.toList());
	}
	
	public static List<String> arrayToList(String[] arr) {
		if (arr == null) return new ArrayList<>(0);
		return Arrays.stream(arr).collect(Collectors.toList());
	}
	
	public static String[] listToArray(List<String> list) {
		if (list == null) return new String[0];
		return list.toArray(String[]::new);
	}
	
	public static String valueOf(Object o) {
		if (o == null) return "";
		return o.toString();
	}
}
