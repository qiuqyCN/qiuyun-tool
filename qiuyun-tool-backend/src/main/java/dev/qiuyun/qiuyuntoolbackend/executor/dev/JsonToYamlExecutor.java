package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JSON转YAML工具执行器
 * 支持：JSON转YAML、YAML转JSON
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonToYamlExecutor implements ToolExecutor<JsonToYamlExecutor.JsonToYamlRequest, JsonToYamlExecutor.JsonToYamlResponse> {

    private final ObjectMapper objectMapper;
    private final YAMLMapper yamlMapper;

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
        if (request == null || request.getInput() == null || request.getInput().trim().isEmpty()) {
            throw new BusinessException("输入内容不能为空");
        }
        if (request.getOperation() == null || request.getOperation().trim().isEmpty()) {
            throw new BusinessException("操作类型不能为空");
        }
        // 验证操作类型
        if (!"json-to-yaml".equals(request.getOperation()) && !"yaml-to-json".equals(request.getOperation())) {
            throw new BusinessException("不支持的操作类型: " + request.getOperation());
        }
    }

    @Override
    public JsonToYamlResponse execute(JsonToYamlRequest request, ToolContext context) throws BusinessException {
        String input = request.getInput().trim();
        String operation = request.getOperation();

        try {
            String result;
            if ("json-to-yaml".equals(operation)) {
                result = jsonToYaml(input);
            } else if ("yaml-to-json".equals(operation)) {
                result = yamlToJson(input);
            } else {
                throw new BusinessException("不支持的操作类型: " + operation);
            }

            JsonToYamlResponse response = new JsonToYamlResponse();
            response.setSuccess(true);
            response.setResult(result);
            response.setOperation(operation);
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("转换错误: {}", e.getMessage());
            throw new BusinessException("转换失败: " + e.getMessage());
        }
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
    @Data
    public static class JsonToYamlResponse {
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 转换结果
         */
        private String result;
        /**
         * 操作类型
         */
        private String operation;
        /**
         * 错误信息（失败时）
         */
        private String errorMessage;
    }
}
