package dev.qiuyun.qiuyuntoolbackend.executor.crypto;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MD5加密工具执行器
 * 支持：32位/16位MD5、大小写输出、批量加密
 */
@Slf4j
@Component
public class Md5EncryptExecutor implements ToolExecutor<Md5EncryptExecutor.Md5Request, Md5EncryptExecutor.Md5Response> {

    @Override
    public String getToolCode() {
        return "md5-encrypt";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(Md5Request request) throws BusinessException {
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getInput() == null || request.getInput().isEmpty()) {
            throw new BusinessException("输入内容不能为空");
        }
    }

    @Override
    public Md5Response execute(Md5Request request, ToolContext context) throws BusinessException {
        try {
            String input = request.getInput();
            int bitLength = request.getBitLength() != null ? request.getBitLength() : 32;
            boolean uppercase = request.getUppercase() != null ? request.getUppercase() : false;
            String charset = request.getCharset() != null ? request.getCharset() : "UTF-8";

            Md5Response response = new Md5Response();
            response.setSuccess(true);
            response.setBitLength(bitLength);
            response.setUppercase(uppercase);

            // 处理多行输入（批量加密）
            String[] lines = input.split("\\r?\\n");
            List<Md5Result> results = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                Md5Result result = new Md5Result();
                result.setInput(line);
                result.setOutput(encryptMd5(line, bitLength, uppercase, charset));
                results.add(result);
            }

            response.setResults(results);
            response.setCount(results.size());

            return response;

        } catch (Exception e) {
            log.error("MD5加密错误: {}", e.getMessage());
            throw new BusinessException("加密失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "MD5加密",
                "description", "MD5加密工具，支持32位/16位",
                "bitLengths", new Integer[]{32, 16},
                "charsets", new String[]{"UTF-8", "GBK", "ISO-8859-1", "ASCII"},
                "features", new String[]{"uppercase", "lowercase", "batch"}
        );
    }

    /**
     * MD5加密
     */
    private String encryptMd5(String input, int bitLength, boolean uppercase, String charset)
            throws NoSuchAlgorithmException, java.io.UnsupportedEncodingException {

        // 获取字节数组
        byte[] inputBytes;
        switch (charset.toUpperCase()) {
            case "GBK":
                inputBytes = input.getBytes("GBK");
                break;
            case "ISO-8859-1":
                inputBytes = input.getBytes(StandardCharsets.ISO_8859_1);
                break;
            case "ASCII":
                inputBytes = input.getBytes(StandardCharsets.US_ASCII);
                break;
            case "UTF-8":
            default:
                inputBytes = input.getBytes(StandardCharsets.UTF_8);
                break;
        }

        // MD5加密
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(inputBytes);

        // 转换为十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() < 2) {
                sb.append('0');
            }
            sb.append(hex);
        }

        String result = sb.toString();

        // 16位MD5（取中间16位）
        if (bitLength == 16) {
            result = result.substring(8, 24);
        }

        // 大小写转换
        if (uppercase) {
            result = result.toUpperCase();
        } else {
            result = result.toLowerCase();
        }

        return result;
    }

    /**
     * 请求参数
     */
    @Data
    public static class Md5Request {
        /**
         * 输入内容
         */
        private String input;
        /**
         * 位数：32或16
         */
        private Integer bitLength = 32;
        /**
         * 是否大写输出
         */
        private Boolean uppercase = false;
        /**
         * 字符集
         */
        private String charset = "UTF-8";
    }

    /**
     * 响应结果
     */
    @Data
    public static class Md5Response {
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 位数
         */
        private int bitLength;
        /**
         * 是否大写
         */
        private boolean uppercase;
        /**
         * 结果列表
         */
        private List<Md5Result> results;
        /**
         * 结果数量
         */
        private int count;
        /**
         * 错误信息
         */
        private String errorMessage;
    }

    /**
     * 单个MD5结果
     */
    @Data
    public static class Md5Result {
        /**
         * 输入内容
         */
        private String input;
        /**
         * MD5结果
         */
        private String output;
    }
}
