package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.qiuyun.qiuyuntoolbackend.enums.JsonOperation;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JSON格式化工具执行器
 * 支持：格式化、压缩、转义、去转义
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonFormatterExecutor extends AbstractToolExecutor<JsonFormatterExecutor.JsonFormatRequest, JsonFormatterExecutor.JsonFormatResponse> {

    private final ObjectMapper objectMapper;

    private static final Set<String> VALID_OPERATIONS = Stream.of(JsonOperation.values())
            .map(JsonOperation::getCode)
            .collect(Collectors.toSet());

    @Override
    public String getToolCode() {
        return "json-formatter";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(JsonFormatRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getInput(), "输入内容");
        validateNotEmpty(request.getOperation(), "操作类型");
        validateEnum(request.getOperation(), "操作类型", VALID_OPERATIONS);
    }

    @Override
    protected JsonFormatResponse doExecute(JsonFormatRequest request, ToolContext context) throws Exception {
        JsonOperation operation = JsonOperation.fromCode(request.getOperation());
        String input = request.getInput().trim();

        String result;
        switch (operation) {
            case FORMAT:
                result = formatJson(input);
                break;
            case COMPRESS:
                result = compressJson(input);
                break;
            case ESCAPE:
                result = escapeJson(input);
                break;
            case UNESCAPE:
                result = unescapeJson(input);
                break;
            default:
                throw new BusinessException("不支持的操作类型: " + request.getOperation());
        }

        JsonFormatResponse response = new JsonFormatResponse();
        response.setSuccess(true);
        response.setResult(result);
        response.setOperation(operation.getCode());
        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        if (e instanceof JsonProcessingException) {
            return "JSON格式错误: " + e.getMessage();
        }
        return "处理失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "JSON格式化",
                "description", "JSON数据的格式化、压缩、转义等操作",
                "operations", new String[]{
                    JsonOperation.FORMAT.getCode(),
                    JsonOperation.COMPRESS.getCode(),
                    JsonOperation.ESCAPE.getCode(),
                    JsonOperation.UNESCAPE.getCode()
                },
                "operationLabels", Map.of(
                        JsonOperation.FORMAT.getCode(), JsonOperation.FORMAT.getLabel(),
                        JsonOperation.COMPRESS.getCode(), JsonOperation.COMPRESS.getLabel(),
                        JsonOperation.ESCAPE.getCode(), JsonOperation.ESCAPE.getLabel(),
                        JsonOperation.UNESCAPE.getCode(), JsonOperation.UNESCAPE.getLabel()
                )
        );
    }

    /**
     * 格式化JSON（美化输出）
     */
    private String formatJson(String input) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(input);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    /**
     * 压缩JSON（去除空白）
     */
    private String compressJson(String input) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(input);
        return objectMapper.writeValueAsString(jsonNode);
    }

    /**
     * 转义JSON（将JSON字符串转义）
     */
    private String escapeJson(String input) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(input);
        String jsonString = objectMapper.writeValueAsString(jsonNode);
        // 将JSON字符串再次JSON序列化，实现转义
        return objectMapper.writeValueAsString(jsonString);
    }

    /**
     * 去转义JSON（将转义的JSON字符串还原）
     */
    private String unescapeJson(String input) throws JsonProcessingException {
        // 首先解析外层JSON，应该得到一个字符串
        JsonNode outerNode = objectMapper.readTree(input);
        if (!outerNode.isTextual()) {
            throw new BusinessException("输入不是有效的转义JSON字符串");
        }
        String innerJson = outerNode.asText();
        // 解析内层JSON并格式化输出
        JsonNode innerNode = objectMapper.readTree(innerJson);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(innerNode);
    }

    /**
     * 请求参数
     */
    @Data
    public static class JsonFormatRequest {
        /**
         * 操作类型：format(格式化)、compress(压缩)、escape(转义)、unescape(去转义)
         */
        private String operation;
        /**
         * 输入的JSON字符串
         */
        private String input;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class JsonFormatResponse extends BaseToolResponse {
        /**
         * 处理结果
         */
        private String result;
        /**
         * 操作类型
         */
        private String operation;
    }
}
