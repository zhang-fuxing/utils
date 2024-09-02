package com.zhangfuxing.tools.net;

import java.nio.charset.StandardCharsets;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/21
 * @email zhangfuxing1010@163.com
 */
public class NetUtil {

    public static String stringEncode(String str) {
        return java.net.URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    public static String stringDecode(String str) {
        return java.net.URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

    public static String urlEncode(String url, String... params) {
        StringBuilder sb = new StringBuilder(url);
        if (params != null && params.length > 0 && params.length % 2 == 0) {
            sb.append("?");
            for (int i = 0; i < params.length; i += 2) {
                sb.append(stringEncode(params[i])).append("=").append(stringEncode(params[i + 1])).append("&");
            }
        }
        return sb.toString();
    }

    public static String urlDecode(String url) {
        return java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
    }

}
