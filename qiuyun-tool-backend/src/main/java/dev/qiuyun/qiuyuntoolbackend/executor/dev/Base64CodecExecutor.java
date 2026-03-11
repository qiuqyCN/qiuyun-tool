package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Base64编解码工具执行器
 * 支持：Base64编码、Base64解码、URL安全Base64
 */
@Slf4j
@Component
public class Base64CodecExecutor implements ToolExecutor<Base64CodecExecutor.Base64Request, Base64CodecExecutor.Base64Response> {

    @Override
    public String getToolCode() {
        return "base64-codec";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(Base64Request request) throws BusinessException {
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getOperation() == null || request.getOperation().trim().isEmpty()) {
            throw new BusinessException("操作类型不能为空");
        }
        if (!"encode".equals(request.getOperation()) && !"decode".equals(request.getOperation())) {
            throw new BusinessException("不支持的操作类型: " + request.getOperation());
        }
        if (request.getInput() == null || request.getInput().isEmpty()) {
            throw new BusinessException("输入内容不能为空");
        }
    }

    @Override
    public Base64Response execute(Base64Request request, ToolContext context) throws BusinessException {
        String operation = request.getOperation();
        String input = request.getInput();
        String charset = request.getCharset() != null ? request.getCharset() : "UTF-8";
        boolean urlSafe = request.isUrlSafe();

        try {
            Base64Response response = new Base64Response();
            response.setSuccess(true);
            response.setOperation(operation);
            response.setInput(input);

            if ("encode".equals(operation)) {
                // Base64编码
                String result = encodeBase64(input, charset, urlSafe);
                response.setOutput(result);
                response.setInputLength(input.length());
                response.setOutputLength(result.length());
            } else if ("decode".equals(operation)) {
                // Base64解码
                String result = decodeBase64(input, charset, urlSafe);
                response.setOutput(result);
                response.setInputLength(input.length());
                response.setOutputLength(result.length());
            }

            return response;

        } catch (IllegalArgumentException e) {
            log.error("Base64解码错误: {}", e.getMessage());
            throw new BusinessException("解码失败: 无效的Base64字符串");
        } catch (Exception e) {
            log.error("Base64编解码错误: {}", e.getMessage());
            throw new BusinessException("操作失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "Base64编解码",
                "description", "Base64编码和解码工具",
                "operations", new String[]{"encode", "decode"},
                "operationLabels", Map.of(
                        "encode", "编码 (Encode)",
                        "decode", "解码 (Decode)"
                ),
                "charsets", new String[]{"UTF-8", "GBK", "ISO-8859-1", "ASCII"},
                "features", new String[]{"urlSafe", "lineBreak"}
        );
    }

    /**
     * Base64编码
     */
    private String encodeBase64(String input, String charset, boolean urlSafe) {
        byte[] inputBytes;
        
        // 根据字符集获取字节数组
        switch (charset.toUpperCase()) {
            case "GBK":
                inputBytes = input.getBytes(java.nio.charset.Charset.forName("GBK"));
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

        // 选择编码器
        Base64.Encoder encoder = urlSafe 
                ? Base64.getUrlEncoder() 
                : Base64.getEncoder();

        return encoder.encodeToString(inputBytes);
    }

    /**
     * Base64解码
     */
    private String decodeBase64(String input, String charset, boolean urlSafe) {
        // 清理输入（移除换行符和空格）
        String cleanInput = input.replaceAll("\\s", "");

        // 选择解码器
        Base64.Decoder decoder = urlSafe 
                ? Base64.getUrlDecoder() 
                : Base64.getDecoder();

        byte[] decodedBytes = decoder.decode(cleanInput);

        // 根据字符集转换为字符串
        switch (charset.toUpperCase()) {
            case "GBK":
                return new String(decodedBytes, java.nio.charset.Charset.forName("GBK"));
            case "ISO-8859-1":
                return new String(decodedBytes, StandardCharsets.ISO_8859_1);
            case "ASCII":
                return new String(decodedBytes, StandardCharsets.US_ASCII);
            case "UTF-8":
            default:
                return new String(decodedBytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * 请求参数
     */
    @Data
    public static class Base64Request {
        /**
         * 操作类型：encode(编码)、decode(解码)
         */
        private String operation;
        /**
         * 输入内容
         */
        private String input;
        /**
         * 字符集：UTF-8、GBK、ISO-8859-1、ASCII
         */
        private String charset = "UTF-8";
        /**
         * 是否使用URL安全Base64（将+替换为-，/替换为_）
         */
        private boolean urlSafe = false;
    }

    /**
     * 响应结果
     */
    @Data
    public static class Base64Response {
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 操作类型
         */
        private String operation;
        /**
         * 输入内容
         */
        private String input;
        /**
         * 输出内容
         */
        private String output;
        /**
         * 输入长度
         */
        private int inputLength;
        /**
         * 输出长度
         */
        private int outputLength;
        /**
         * 错误信息
         */
        private String errorMessage;
    }
}
