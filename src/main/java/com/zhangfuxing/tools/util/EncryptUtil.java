package com.zhangfuxing.tools.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/27
 * @email zhangfuxing1010@163.com
 */
public class EncryptUtil {

    /**
     * 对使用 DESede 加密的字符串进行解密, 默认使用 16 位 HASH 结果的前 8 位进行填充
     *
     * @param content 解密内容
     * @param key     密钥
     * @return 解密结果
     */
    public static String decrypt3DES(String content, String key) {
        return decrypt3DESBefore8Padding(content, key);
    }

    /**
     * 对使用 DESede 加密的字符串进行解密
     * <p>
     * 该算法将使用MD5对密钥进HASH, 将HASH后的结果作为密钥进行加密, 加密模式为ECB, 填充模式为PKCS5Padding
     * <p>
     * 密钥长度位24字节, 前16字节为MD5后的结果, 后8字节为MD5后的结果的 <a style="color:red">前8位</a>字节进行填充
     *
     * @param content 解密内容
     * @param key     密钥
     * @return 解密结果
     */
    public static String decrypt3DESBefore8Padding(String content, String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] keyBytes = digest.digest(StandardCharsets.US_ASCII.encode(key).array());
            assert keyBytes.length == 16;
            // 创建24字节的密钥 后8位截取 keyBytes 的前8位进行填充
            byte[] keyBytes24 = new byte[24];
            System.arraycopy(keyBytes, 0, keyBytes24, 0, 16);
            System.arraycopy(keyBytes, 0, keyBytes24, 16, 8);
            SecretKey secretKey = new SecretKeySpec(keyBytes24, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(content));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }


    /**
     * 对使用 DESede 加密的字符串进行解密
     * <p>
     * 该算法将使用MD5对密钥进HASH, 将HASH后的结果作为密钥进行加密, 加密模式为ECB, 填充模式为PKCS5Padding
     * <p>
     * 密钥长度位24字节, 前16字节为MD5后的结果, 后8字节为MD5后的结果的 <a style="color:red">后8位</a> 字节进行填充
     *
     * @param content 解密内容
     * @param key     密钥  重复
     * @return 解密结果
     */
    public static String decrypt3DESAfter8Padding(String content, String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] keyBytes = digest.digest(StandardCharsets.US_ASCII.encode(key).array());
            assert keyBytes.length == 16;
            // 创建24字节的密钥
            byte[] keyBytes24 = new byte[24];
            System.arraycopy(keyBytes, 0, keyBytes24, 0, 16);
            System.arraycopy(keyBytes, 8, keyBytes24, 16, 8);
            SecretKey secretKey = new SecretKeySpec(keyBytes24, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(content));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 对使用 DESede 加密的字符串进行解密
     * <p>
     * 该算法将使用MD5对密钥进HASH, 将HASH后的结果作为密钥进行加密, 加密模式为ECB, 填充模式为PKCS5Padding
     * <p>
     * 密钥长度位24字节, 前16字节为MD5后的结果, 后8字节为 <a style="color:red">0填充</a>
     *
     * @param content 解密内容
     * @param key     密钥  重复
     * @return 解密结果
     */
    public static String decrypt3DESZeroPadding(String content, String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] keyBytes = digest.digest(StandardCharsets.US_ASCII.encode(key).array());
            assert keyBytes.length == 16;
            // 创建24字节的密钥
            byte[] keyBytes24 = new byte[24];
            System.arraycopy(keyBytes, 0, keyBytes24, 0, 16);
            SecretKey secretKey = new SecretKeySpec(keyBytes24, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(content));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
}
