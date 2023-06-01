package com.zhangfuxing.tools;

import com.zhangfuxing.tools.enums.JoinStr;
import com.zhangfuxing.tools.exception.IllegalFieldException;
import com.zhangfuxing.tools.exception.StringJoinModelException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtil {
    public static final int INDEX_NOT_FOUND = -1;
    public static final int BUFFER = 1;
    public static final int BUILDER = 2;


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
        return Arrays.stream(args).filter(StringUtil::isEmpty).toArray(String[]::new);
    }

    public static List<String> findNotNullStrList(String... args) {
        if (args == null) return new ArrayList<>(0);
        return Arrays.stream(args).filter(StringUtil::isEmpty).collect(Collectors.toList());
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
                    } catch (NumberFormatException e) {
//                        throw new IllegalArgumentException("字符串格式不正确，索引不是有效的数字: " + indexStr);
                    }
                }
                sb.replace(index, endIndex + 1, argStr);
                argIndex++;
            }
            index++;
        }
        return sb.toString();
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
}
