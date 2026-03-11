package dev.qiuyun.qiuyuntoolbackend.executor.crypto;

import dev.qiuyun.qiuyuntoolbackend.constant.ToolConstants;
import dev.qiuyun.qiuyuntoolbackend.enums.OperationType;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import dev.qiuyun.qiuyuntoolbackend.executor.common.InputOutputResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * URL编解码工具执行器
 * 支持：URL编码、URL解码、批量处理、多种字符集
 */
@Slf4j
@Component
public class UrlCodecExecutor extends AbstractToolExecutor<UrlCodecExecutor.UrlCodecRequest, UrlCodecExecutor.UrlCodecResponse> {

    private static final Set<String> VALID_OPERATIONS = Set.of(OperationType.ENCODE.getCode(), OperationType.DECODE.getCode());

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
        validateNotNull(request, "请求");
        validateNotEmpty(request.getOperation(), "操作类型");
        validateEnum(request.getOperation(), "操作类型", VALID_OPERATIONS);
        validateNotEmpty(request.getInput(), "输入内容");
    }

    @Override
    protected UrlCodecResponse doExecute(UrlCodecRequest request, ToolContext context) throws Exception {
        String operation = request.getOperation();
        String input = request.getInput();
        String charset = request.getCharset() != null ? request.getCharset() : ToolConstants.DEFAULT_CHARSET;
        boolean encodeSpaceAsPlus = request.isEncodeSpaceAsPlus();

        UrlCodecResponse response = new UrlCodecResponse();
        response.setSuccess(true);
        response.setOperation(operation);
        response.setCharset(charset);

        // 处理多行输入（批量处理）
        String[] lines = input.split("\\r?\\n");
        List<InputOutputResult> results = new ArrayList<>();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            InputOutputResult result = new InputOutputResult();
            result.setInput(line);

            if (OperationType.ENCODE.getCode().equals(operation)) {
                result.setOutput(urlEncode(line, charset, encodeSpaceAsPlus));
            } else {
                result.setOutput(urlDecode(line, charset));
            }

            results.add(result);
        }

        response.setResults(results);
        response.setCount(results.size());

        return response;
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "URL编解码",
                "description", "URL编码和解码工具",
                "operations", new String[]{OperationType.ENCODE.getCode(), OperationType.DECODE.getCode()},
                "operationLabels", Map.of(
                        OperationType.ENCODE.getCode(), OperationType.ENCODE.getLabel(),
                        OperationType.DECODE.getCode(), OperationType.DECODE.getLabel()
                ),
                "charsets", ToolConstants.COMMON_CHARSETS,
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
        private String charset = ToolConstants.DEFAULT_CHARSET;
        /**
         * 是否将空格编码为+（否则编码为%20）
         */
        private boolean encodeSpaceAsPlus = true;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class UrlCodecResponse extends BaseToolResponse {
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
        private List<InputOutputResult> results;
        /**
         * 结果数量
         */
        private int count;
    }
}
