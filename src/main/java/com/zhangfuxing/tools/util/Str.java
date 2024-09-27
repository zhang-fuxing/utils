package com.zhangfuxing.tools.util;

import com.zhangfuxing.tools.common.enums.JoinStr;
import com.zhangfuxing.tools.common.exception.IllegalFieldException;
import com.zhangfuxing.tools.common.exception.StringJoinModelException;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Str {
    public static final String DELIMITER = ",";
    private final String value;
    public static final int INDEX_NOT_FOUND = -1;
    public static final int BUFFER = 1;
    public static final int BUILDER = 2;

    public Str() {
        value = "";
    }

    public Str(String value, Object... args) {
        this.value = format(value, args);
    }

    public String instanceFmt(Object... args) {
        return format(this.value, args);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean notEmpty(String s) {
        return !isEmpty(s);
    }

    public static String requireNonNullElseGet(String pattern, String s) {
        return notEmpty(pattern) ? pattern : s;
    }

    public static String requireNonNullElseGet(Object obj, String s) {
        return obj == null ? s : obj.toString();
    }

    public static boolean fieldCheck(String str) {
        String pattern = "^((?!['/*\\-]).)*$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        return m.matches();
    }

    public static void isIllegalField(String str) {
        if (isBlank(str)) return;
        if (!fieldCheck(str)) throw new IllegalFieldException("不能包含特殊字符");
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
        return Arrays.stream(args).filter(Str::isEmpty).toArray(String[]::new);
    }

    public static List<String> findNotNullStrList(String... args) {
        if (args == null) return new ArrayList<>(0);
        return Arrays.stream(args).filter(Str::isEmpty).collect(Collectors.toList());
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

    static final char[] s = "0123456789abcdef".toCharArray();
    static final char[] d = "0123456789ABCDEF".toCharArray();

    public static StringBuilder appendBuilder(JoinStr joinStr, String... args) {
        var builder = new StringBuilder();
        if (args == null) {
            return builder;
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            builder.append(arg);
            if (joinStr != null && i != args.length - 1) {
                builder.append(joinStr.getCharacter());
            }
        }
        return builder;
    }

    public static StringBuffer appendBuffer(JoinStr joinStr, String... args) {
        var builder = new StringBuffer();
        if (args == null) {
            return builder;
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            builder.append(arg);
            if (joinStr != null && i != args.length - 1) {
                builder.append(joinStr.getCharacter());
            }
        }
        return builder;
    }

    public static String join(int model, JoinStr joinStr, String... args) {
        if (BUFFER == model) return appendBuffer(joinStr, args).toString();
        if (BUILDER == model) return appendBuilder(joinStr, args).toString();
        throw new StringJoinModelException("未知的字符串连接模式");
    }

    public static String getClassPath() {
        return Thread.currentThread().getContextClassLoader().getResource("").getPath();
    }

    public static String join(int model, String... args) {
        return join(model, null, args);
    }

    public static String join(JoinStr joinStr, String... args) {
        return join(BUILDER, joinStr, args);
    }

    public static String join(String... args) {
        return join(BUILDER, null, args);
    }

    public static String toMD5(String str, Charset charset, boolean isUpperCase) throws NoSuchAlgorithmException {
        byte[] bytes = MD5Util.getMd5Bytes(str.getBytes(charset));
        char[] chars = encode(bytes, isUpperCase);
        return String.valueOf(chars);
    }

    public static String toMD5(String str, Charset charset) throws NoSuchAlgorithmException {
        return toMD5(str, charset, true);
    }

    public static char[] encode(byte[] bytes, boolean isUpperCase) {
        int len = bytes.length;
        final char[] out = new char[len << 1];
        char[] encode = getEncode(isUpperCase);
        for (int i = 0, j = 0; i < len; i++) {
            out[j++] = encode[(0xF0 & bytes[i]) >>> 4];
            out[j++] = encode[0x0f & bytes[i]];
        }
        return out;
    }

    private static char[] getEncode(boolean isUpperCase) {
        return isUpperCase ? d : s;
    }

    public static String format(String str, Object... args) {
        if (str == null) {
            throw new NullPointerException("模板字符串不能为空");
        }
        if (str.isBlank()) {
            return "";
        }
        if (args == null || args.length == 0) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        int index = 0;
        int argIndex = 0;
        while ((index = sb.indexOf("{", index)) != -1) {
            if (index == 0 || sb.charAt(index - 1) != '\\') {
                int endIndex = sb.indexOf("}", index + 1);
                if (endIndex == -1) {
                    throw new IllegalArgumentException("字符串格式不正确，缺少'}'");
                }
                String argStr = Objects.toString(args[argIndex], "");
                String indexStr = sb.substring(index + 1, endIndex);
                if (!indexStr.isBlank()) {
                    try {
                        int argIndexTemp = Integer.parseInt(indexStr);
                        if (argIndexTemp >= 0 && argIndexTemp < args.length) {
                            argStr = Objects.toString(args[argIndexTemp], "");
                        } else {
                            throw new IllegalArgumentException("参数索引超出范围: " + argIndexTemp);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
                sb.replace(index, endIndex + 1, argStr);
                argIndex++;
            }
            index++;
        }

        String result = sb.toString();
        result = result.replace("\\{", "{");
        return result;
    }

    public static void println(String str, Object... args) {
        System.out.println(format(str, args));
    }

    public static byte[] getBytes(String str) {
        if (isBlank(str)) return null;
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static String getStr(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 字符串循环，给的一个字符串和循环次数，将拼接n次字符串
     *
     * @param str 目标字符串
     * @param c   次数
     * @return 循环后的字符串
     */
    public static String circu(String str, int c) {
        for (int i = 0; i < c - 1; i++) {
            str += str;
        }
        return str;
    }

    public static Str newInstance(String value, Object... args) {
        return new Str(value, args);
    }

    public static String joiner1(CharSequence delimiter, CharSequence prefix, CharSequence suffix, CharSequence... elements) {
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
        if (elements != null) {
            for (CharSequence element : elements) {
                joiner.add(element);
            }
        }
        return joiner.toString();
    }

    public static String joiner(CharSequence delimiter, CharSequence... elements) {
        StringJoiner joiner = new StringJoiner(delimiter);
        if (elements != null) {
            for (CharSequence element : elements) {
                if (isBlank(element)) {
                    continue;
                }
                joiner.add(element);
            }
        }
        return joiner.toString();
    }

    public static String toStr(Collection<?> data, CharSequence delimiter) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        return data.stream()
                .map(Str::valueOf)
                .collect(Collectors.joining(delimiter));
    }

    public static boolean isNull(String str) {
        return str == null || str.isEmpty();
    }

    public boolean isNull() {
        return isNull(this.value);
    }

    public static String fmt(String str, Object... args) {
        return new Str(str, args).value;
    }

    public static String fmt(CharSequence template, Map<?, ?> map, boolean ignoreNull) {
        return format(valueOf(template), map, ignoreNull);
    }

    public static String fmt(CharSequence template, Map<?, ?> map) {
        return fmt(template, map, true);
    }


    public String get() {
        return this.value;
    }

    public byte[] getBytes() {
        return this.value.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getUTF8Bytes() {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public static String toLowerCase(String str) {
        return str == null ? "" : str.toLowerCase();
    }

    public static String toUpperCase(String str) {
        return str == null ? "" : str.toUpperCase();
    }

    public static String fmtUrlEncode(String url, String... args) {
        if (args == null || args.length == 0) {
            return url;
        }
        var array = Arrays.stream(args).map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8)).toArray();
        return fmt(url, array);
    }

    public static String URLEncode(String param) {
        return URLEncoder.encode(param, StandardCharsets.UTF_8);
    }

    public static String replace(String docRoot, String oldStr, String newStr) {
        if (isNull(docRoot)) {
            return "";
        }
        return docRoot.replace(oldStr, newStr);
    }

    public static String convertSeparator(String path) {
        return replace(path, "\\", "/");
    }

    public static boolean endWith(String str, String suffix, boolean ignoreCase, boolean ignoreEquals) {
        if (null == str || null == suffix) {
            if (ignoreEquals) {
                return false;
            }
            return null == str && null == suffix;
        }

        final int strOffset = str.length() - suffix.length();
        boolean isEndWith = str
                .regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());

        if (isEndWith) {
            return (!ignoreEquals) || (!equals(str, suffix, ignoreCase));
        }
        return false;
    }

    public static boolean endWith(String str, String suffix) {
        return endWith(str, suffix, false, false);
    }

    public static boolean startWith(String str, String suffix) {
        return startWith(str, suffix, false, false);
    }

    public static boolean startWith(String str, String prefix, boolean ignoreCase, boolean ignoreEquals) {
        if (null == str || null == prefix) {
            if (ignoreEquals) {
                return false;
            }
            return null == str && null == prefix;
        }

        boolean isStartWith = str
                .regionMatches(ignoreCase, 0, prefix, 0, prefix.length());

        if (isStartWith) {
            return (!ignoreEquals) || (!equals(str, prefix, ignoreCase));
        }
        return false;
    }

    public static String appendURI(String... uris) {
        if (uris == null || uris.length == 0) {
            return "";
        }
        String res = "";
        for (String uri : uris) {
            if (res.isBlank()) {
                res = uri;
                continue;
            }
            if (!endWith(res, "/")) {
                res += "/";
            }
            if (startWith(uri, "/")) {
                uri = uri.substring(1);
            }
            res += uri;
        }
        return res;
    }

    public static boolean equals(String str1, String str2) {
        return equals(str1, str2, false);
    }

    public static boolean equals(String str1, String str2, boolean ignoreCase) {
        boolean result;
        if (null == str1) {
            // 只有两个都为null才判断相等
            result = str2 == null;
        } else if (null == str2) {
            // 字符串2空，字符串1非空，直接false
            result = false;
        } else if (ignoreCase) {
            result = str1.equalsIgnoreCase(str2);
        } else {
            result = str1.contentEquals(str2);
        }

        return result;
    }

    public static String replaceMatch(String source, String pattern, String str) {
        if (isBlank(source)) {
            return "";
        }
        return source.replaceAll(pattern, str);
    }

    public static int len(String str) {
        return str == null ? 0 : str.length();
    }

    /**
     * 根据给定的条件，截取字符串中指定范围的部分。
     *
     * @param str   要截取的字符串
     * @param start 起始位置
     * @param end   结束位置
     * @param with  截取满足指定条件的字符
     * @return 截取后的字符串
     */
    public static String substring(String str, int start, int end, Predicate<String> with) {
        return substring(str, start, end, with, len(str));
    }

    /**
     * 根据给定的参数截取字符串的子串
     *
     * @param str     要截取的字符串
     * @param start   子串的起始位置
     * @param end     子串的结束位置
     * @param with    正则表达式，用于判断字符串是否满足条件。如果不满足条件则按照elseEnd位置截取
     * @param elseEnd 当字符串不满足with条件时，指定的结束位置
     * @return 截取后的子串
     */
    public static String substring(String str, int start, int end, Predicate<String> with, Integer elseEnd) {
        if (len(str) == 0) {
            return "";
        }
        if (with != null && !with.test(str)) {
            return str.substring(start, elseEnd);
        }
        return str.substring(start, end);
    }

    public static String substring(String str, int start, int end) {
        if (len(str) == 0) {
            return "";
        }
        return substring(str, start, end, null);
    }

    public static String substring(String str, int start, Predicate<String> with) {
        int len = len(str);
        if (len == 0) {
            return "";
        }
        return substring(str, start, len, with);
    }

    public static String substring(String str, int start) {
        int len = len(str);
        if (len == 0) {
            return "";
        }
        return substring(str, start, len, null);
    }

    public static byte[] utf8Bytes(String str) {
        return new Str(str).getUTF8Bytes();
    }

    public static byte[] hexToByte(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteToHex(Byte[] byteArray) {
        StringBuilder result = new StringBuilder("0x");
        for (byte b : byteArray) {
            result.append(String.format("%02X", b & 0xFF));
        }
        return result.toString();
    }

    public static String byteToHex(byte[] byteArray) {
        StringBuilder result = new StringBuilder("0x");
        for (byte b : byteArray) {
            result.append(String.format("%02X", b & 0xFF));
        }
        return result.toString();
    }


    public static String toLowerCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 去除字符串两端的空格
        input = input.trim();

        // 使用StringBuilder来高效地构建字符串
        StringBuilder camelCaseString = new StringBuilder();
        boolean capitalizeNext = false;

        // 遍历输入字符串的每个字符
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            // 如果当前字符是分隔符（这里假设空格、下划线等均为分隔符）
            // 或者这是字符串的第一个字符但不是小写（因为需要特殊处理全大写的情况）
            if (Character.isWhitespace(currentChar) || currentChar == '_' || (i == 0 && Character.isUpperCase(currentChar))) {
                capitalizeNext = true;

                // 如果是分隔符，则跳过它
                if (Character.isWhitespace(currentChar) || currentChar == '_') {
                    continue;
                }
            }

            // 根据capitalizeNext的值来决定是否大写当前字符
            if (capitalizeNext) {
                camelCaseString.append(Character.toUpperCase(currentChar));
                capitalizeNext = false; // 重置标记
            } else {
                // 如果不是第一个字符且需要小写（对于全大写字符串的第一个字符后的处理）
                if (i > 0) {
                    camelCaseString.append(Character.toLowerCase(currentChar));
                } else {
                    // 第一个字符直接添加（此时为小写，因为全大写情况会在后续被处理）
                    camelCaseString.append(currentChar);
                }
            }
        }

        // 如果输入字符串全是大写，并且没有分隔符，我们需要将第一个字符转为小写
        if (camelCaseString.length() > 1 && camelCaseString.charAt(0) == camelCaseString.toString().toUpperCase().charAt(0)) {
            camelCaseString.setCharAt(0, Character.toLowerCase(camelCaseString.charAt(0)));
        }

        return camelCaseString.toString();
    }

}
