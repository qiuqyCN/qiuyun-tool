package dev.qiuyun.qiuyuntoolbackend.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 字符集工具类
 * 提供字符串与字节数组的相互转换，支持多种字符集
 */
@Component
public class CharsetUtils {

    /**
     * 将字符串按指定字符集编码为字节数组
     *
     * @param input   输入字符串
     * @param charset 字符集名称（UTF-8、GBK、ISO-8859-1、ASCII）
     * @return 字节数组
     */
    public byte[] getBytes(String input, String charset) {
        if (input == null) {
            return new byte[0];
        }

        String upperCharset = charset.toUpperCase();
        if ("GBK".equals(upperCharset)) {
            return input.getBytes(java.nio.charset.Charset.forName("GBK"));
        } else if ("ISO-8859-1".equals(upperCharset)) {
            return input.getBytes(StandardCharsets.ISO_8859_1);
        } else if ("ASCII".equals(upperCharset)) {
            return input.getBytes(StandardCharsets.US_ASCII);
        } else {
            return input.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * 将字节数组按指定字符集解码为字符串
     *
     * @param bytes   字节数组
     * @param charset 字符集名称（UTF-8、GBK、ISO-8859-1、ASCII）
     * @return 字符串
     */
    public String newString(byte[] bytes, String charset) {
        if (bytes == null) {
            return "";
        }

        String upperCharset = charset.toUpperCase();
        if ("GBK".equals(upperCharset)) {
            return new String(bytes, java.nio.charset.Charset.forName("GBK"));
        } else if ("ISO-8859-1".equals(upperCharset)) {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        } else if ("ASCII".equals(upperCharset)) {
            return new String(bytes, StandardCharsets.US_ASCII);
        } else {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取默认字符集
     *
     * @return 默认字符集名称
     */
    public String getDefaultCharset() {
        return "UTF-8";
    }

    /**
     * 获取支持的字符集列表
     *
     * @return 字符集名称数组
     */
    public String[] getSupportedCharsets() {
        return new String[]{"UTF-8", "GBK", "ISO-8859-1", "ASCII"};
    }
}
