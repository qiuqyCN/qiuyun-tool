package dev.qiuyun.qiuyuntoolbackend.executor.crypto;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * URL编解码工具执行器
 * 支持：URL编码、URL解码、批量处理、多种字符集
 */
@Slf4j
@Component
public class UrlCodecExecutor implements ToolExecutor<UrlCodecExecutor.UrlCodecRequest, UrlCodecExecutor.UrlCodecResponse> {

    @Override
    public String getToolCode() {
        return "url-encode";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(UrlCodecRequest request) throws BusinessException {
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
    public UrlCodecResponse execute(UrlCodecRequest request, ToolContext context) throws BusinessException {
        try {
            String operation = request.getOperation();
            String input = request.getInput();
            String charset = request.getCharset() != null ? request.getCharset() : "UTF-8";
            boolean encodeSpaceAsPlus = request.isEncodeSpaceAsPlus();

            UrlCodecResponse response = new UrlCodecResponse();
            response.setSuccess(true);
            response.setOperation(operation);
            response.setCharset(charset);

            // 处理多行输入（批量处理）
            String[] lines = input.split("\\r?\\n");
            List<UrlCodecResult> results = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                UrlCodecResult result = new UrlCodecResult();
                result.setInput(line);

                if ("encode".equals(operation)) {
                    result.setOutput(urlEncode(line, charset, encodeSpaceAsPlus));
                } else {
                    result.setOutput(urlDecode(line, charset));
                }

                results.add(result);
            }

            response.setResults(results);
            response.setCount(results.size());

            return response;

        } catch (Exception e) {
            log.error("URL编解码错误: {}", e.getMessage());
            throw new BusinessException("操作失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "URL编解码",
                "description", "URL编码和解码工具",
                "operations", new String[]{"encode", "decode"},
                "operationLabels", Map.of(
                        "encode", "编码 (Encode)",
                        "decode", "解码 (Decode)"
                ),
                "charsets", new String[]{"UTF-8", "GBK", "ISO-8859-1", "ASCII"},
                "features", new String[]{"encodeSpaceAsPlus", "batch"}
        );
    }

    /**
     * URL编码
     */
    private String urlEncode(String input, String charset, boolean encodeSpaceAsPlus) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(input, charset);

        // 如果不将空格编码为+，则替换回%20
        if (!encodeSpaceAsPlus) {
            encoded = encoded.replace("+", "%20");
        }

        return encoded;
    }

    /**
     * URL解码
     */
    private String urlDecode(String input, String charset) throws UnsupportedEncodingException {
        // 先处理%20为空格，以便URLDecoder正确处理
        String normalized = input.replace("%20", " ");
        return URLDecoder.decode(normalized, charset);
    }

    /**
     * 请求参数
     */
    @Data
    public static class UrlCodecRequest {
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
         * 是否将空格编码为+（否则编码为%20）
         */
        private boolean encodeSpaceAsPlus = true;
    }

    /**
     * 响应结果
     */
    @Data
    public static class UrlCodecResponse {
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 操作类型
         */
        private String operation;
        /**
         * 字符集
         */
        private String charset;
        /**
         * 结果列表
         */
        private List<UrlCodecResult> results;
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
     * 单个URL编解码结果
     */
    @Data
    public static class UrlCodecResult {
        /**
         * 输入内容
         */
        private String input;
        /**
         * 输出内容
         */
        private String output;
    }
}
