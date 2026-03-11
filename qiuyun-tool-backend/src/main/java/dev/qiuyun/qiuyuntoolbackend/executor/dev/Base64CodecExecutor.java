package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import dev.qiuyun.qiuyuntoolbackend.constant.ToolConstants;
import dev.qiuyun.qiuyuntoolbackend.enums.OperationType;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import dev.qiuyun.qiuyuntoolbackend.util.CharsetUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * Base64编解码工具执行器
 * 支持：Base64编码、Base64解码、URL安全Base64
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Base64CodecExecutor extends AbstractToolExecutor<Base64CodecExecutor.Base64Request, Base64CodecExecutor.Base64Response> {

    private final CharsetUtils charsetUtils;

    private static final Set<String> VALID_OPERATIONS = Set.of(OperationType.ENCODE.getCode(), OperationType.DECODE.getCode());

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
        validateNotNull(request, "请求");
        validateNotEmpty(request.getOperation(), "操作类型");
        validateEnum(request.getOperation(), "操作类型", VALID_OPERATIONS);
        validateNotEmpty(request.getInput(), "输入内容");
    }

    @Override
    protected Base64Response doExecute(Base64Request request, ToolContext context) throws Exception {
        String operation = request.getOperation();
        String input = request.getInput();
        String charset = request.getCharset() != null ? request.getCharset() : ToolConstants.DEFAULT_CHARSET;
        boolean urlSafe = request.isUrlSafe();

        Base64Response response = new Base64Response();
        response.setSuccess(true);
        response.setOperation(operation);
        response.setInput(input);

        if (OperationType.ENCODE.getCode().equals(operation)) {
            String result = encodeBase64(input, charset, urlSafe);
            response.setOutput(result);
            response.setInputLength(input.length());
            response.setOutputLength(result.length());
        } else if (OperationType.DECODE.getCode().equals(operation)) {
            String result = decodeBase64(input, charset, urlSafe);
            response.setOutput(result);
            response.setInputLength(input.length());
            response.setOutputLength(result.length());
        }

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return "解码失败: 无效的Base64字符串";
        }
        return "操作失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "Base64编解码",
                "description", "Base64编码和解码工具",
                "operations", new String[]{OperationType.ENCODE.getCode(), OperationType.DECODE.getCode()},
                "operationLabels", Map.of(
                        OperationType.ENCODE.getCode(), OperationType.ENCODE.getLabel(),
                        OperationType.DECODE.getCode(), OperationType.DECODE.getLabel()
                ),
                "charsets", ToolConstants.COMMON_CHARSETS,
                "features", new String[]{"urlSafe", "lineBreak"}
        );
    }

    /**
     * Base64编码
     */
    private String encodeBase64(String input, String charset, boolean urlSafe) {
        byte[] inputBytes = charsetUtils.getBytes(input, charset);

        Base64.Encoder encoder = urlSafe
                ? Base64.getUrlEncoder()
                : Base64.getEncoder();

        return encoder.encodeToString(inputBytes);
    }

    /**
     * Base64解码
     */
    private String decodeBase64(String input, String charset, boolean urlSafe) {
        String cleanInput = input.replaceAll("\\s", "");

        Base64.Decoder decoder = urlSafe
                ? Base64.getUrlDecoder()
                : Base64.getDecoder();

        byte[] decodedBytes = decoder.decode(cleanInput);

        return charsetUtils.newString(decodedBytes, charset);
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
        private String charset = ToolConstants.DEFAULT_CHARSET;
        /**
         * 是否使用URL安全Base64（将+替换为-，/替换为_）
         */
        private boolean urlSafe = false;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Base64Response extends BaseToolResponse {
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
    }
}
