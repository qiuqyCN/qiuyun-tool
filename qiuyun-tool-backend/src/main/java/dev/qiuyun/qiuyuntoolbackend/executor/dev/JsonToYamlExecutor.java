package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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

/**
 * JSON转YAML工具执行器
 * 支持：JSON转YAML、YAML转JSON
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonToYamlExecutor extends AbstractToolExecutor<JsonToYamlExecutor.JsonToYamlRequest, JsonToYamlExecutor.JsonToYamlResponse> {

    private final ObjectMapper objectMapper;
    private final YAMLMapper yamlMapper;

    private static final Set<String> VALID_OPERATIONS = Set.of("json-to-yaml", "yaml-to-json");

    @Override
    public String getToolCode() {
        return "json-to-yaml";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(JsonToYamlRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getInput(), "输入内容");
        validateNotEmpty(request.getOperation(), "操作类型");
        validateEnum(request.getOperation(), "操作类型", VALID_OPERATIONS);
    }

    @Override
    protected JsonToYamlResponse doExecute(JsonToYamlRequest request, ToolContext context) throws Exception {
        String input = request.getInput().trim();
        String operation = request.getOperation();

        String result;
        if ("json-to-yaml".equals(operation)) {
            result = jsonToYaml(input);
        } else {
            result = yamlToJson(input);
        }

        JsonToYamlResponse response = new JsonToYamlResponse();
        response.setSuccess(true);
        response.setResult(result);
        response.setOperation(operation);
        return response;
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "JSON转YAML",
                "description", "JSON与YAML格式相互转换",
                "operations", new String[]{
                    "json-to-yaml",
                    "yaml-to-json"
                },
                "operationLabels", Map.of(
                        "json-to-yaml", "JSON → YAML",
                        "yaml-to-json", "YAML → JSON"
                )
        );
    }

    /**
     * JSON转YAML
     */
    private String jsonToYaml(String json) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(json);
        return yamlMapper.writeValueAsString(jsonNode);
    }

    /**
     * YAML转JSON
     */
    private String yamlToJson(String yaml) throws Exception {
        JsonNode yamlNode = yamlMapper.readTree(yaml);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(yamlNode);
    }

    /**
     * 请求参数
     */
    @Data
    public static class JsonToYamlRequest {
        /**
         * 操作类型：json-to-yaml、yaml-to-json
         */
        private String operation;
        /**
         * 输入内容
         */
        private String input;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class JsonToYamlResponse extends BaseToolResponse {
        /**
         * 转换结果
         */
        private String result;
        /**
         * 操作类型
         */
        private String operation;
    }
}
