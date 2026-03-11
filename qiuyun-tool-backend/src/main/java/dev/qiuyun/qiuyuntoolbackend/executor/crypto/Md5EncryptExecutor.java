package dev.qiuyun.qiuyuntoolbackend.executor.crypto;

import dev.qiuyun.qiuyuntoolbackend.constant.ToolConstants;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import dev.qiuyun.qiuyuntoolbackend.executor.common.InputOutputResult;
import dev.qiuyun.qiuyuntoolbackend.util.CharsetUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class Md5EncryptExecutor extends AbstractToolExecutor<Md5EncryptExecutor.Md5Request, Md5EncryptExecutor.Md5Response> {

    @Autowired
    private CharsetUtils charsetUtils;

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
        validateNotNull(request, "请求");
        validateNotEmpty(request.getInput(), "输入内容");
    }

    @Override
    protected Md5Response doExecute(Md5Request request, ToolContext context) throws Exception {
        String input = request.getInput();
        int bitLength = request.getBitLength() != null ? request.getBitLength() : ToolConstants.MD5_32BIT;
        boolean uppercase = request.getUppercase() != null ? request.getUppercase() : false;
        String charset = request.getCharset() != null ? request.getCharset() : ToolConstants.DEFAULT_CHARSET;

        Md5Response response = new Md5Response();
        response.setSuccess(true);
        response.setBitLength(bitLength);
        response.setUppercase(uppercase);

        // 处理多行输入（批量加密）
        String[] lines = input.split("\\r?\\n");
        List<InputOutputResult> results = new ArrayList<>();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            InputOutputResult result = new InputOutputResult();
            result.setInput(line);
            result.setOutput(encryptMd5(line, bitLength, uppercase, charset));
            results.add(result);
        }

        response.setResults(results);
        response.setCount(results.size());

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "加密失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "MD5加密",
                "description", "MD5加密工具，支持32位/16位",
                "bitLengths", new Integer[]{ToolConstants.MD5_32BIT, ToolConstants.MD5_16BIT},
                "charsets", ToolConstants.COMMON_CHARSETS,
                "features", new String[]{"uppercase", "lowercase", "batch"}
        );
    }

    /**
     * MD5加密
     */
    private String encryptMd5(String input, int bitLength, boolean uppercase, String charset)
            throws NoSuchAlgorithmException {

        // 使用 CharsetUtils 获取字节数组
        byte[] inputBytes = charsetUtils.getBytes(input, charset);

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
        if (bitLength == ToolConstants.MD5_16BIT) {
            result = result.substring(8, 24);
        }

        // 大小写转换
        return uppercase ? result.toUpperCase() : result.toLowerCase();
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
        private Integer bitLength = ToolConstants.MD5_32BIT;
        /**
         * 是否大写输出
         */
        private Boolean uppercase = false;
        /**
         * 字符集
         */
        private String charset = ToolConstants.DEFAULT_CHARSET;
    }

    /**
     * 响应结果
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Md5Response extends BaseToolResponse {
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
        private List<InputOutputResult> results;
        /**
         * 结果数量
         */
        private int count;
    }
}
