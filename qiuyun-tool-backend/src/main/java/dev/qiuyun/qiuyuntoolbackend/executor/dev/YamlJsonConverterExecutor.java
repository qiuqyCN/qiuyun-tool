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
 * YAML与JSON互转工具执行器
 * 支持：YAML转JSON、JSON转YAML
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YamlJsonConverterExecutor extends AbstractToolExecutor<YamlJsonConverterExecutor.ConverterRequest, YamlJsonConverterExecutor.ConverterResponse> {

    private final ObjectMapper objectMapper;
    private final YAMLMapper yamlMapper;

    private static final Set<String> VALID_OPERATIONS = Set.of("yaml-to-json", "json-to-yaml");

    @Override
    public String getToolCode() {
        return "yaml-json-converter";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(ConverterRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getInput(), "输入内容");
        validateNotEmpty(request.getOperation(), "操作类型");
        validateEnum(request.getOperation(), "操作类型", VALID_OPERATIONS);
    }

    @Override
    protected ConverterResponse doExecute(ConverterRequest request, ToolContext context) throws Exception {
        String input = request.getInput().trim();
        String operation = request.getOperation();

        String result;
        if ("yaml-to-json".equals(operation)) {
            result = yamlToJson(input);
        } else {
            result = jsonToYaml(input);
        }

        ConverterResponse response = new ConverterResponse();
        response.setSuccess(true);
        response.setResult(result);
        response.setOperation(operation);
        return response;
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "YAML/JSON互转",
                "description", "YAML与JSON格式相互转换",
                "operations", new String[]{
                    "yaml-to-json",
                    "json-to-yaml"
                },
                "operationLabels", Map.of(
                        "yaml-to-json", "YAML → JSON",
                        "json-to-yaml", "JSON → YAML"
                )
        );
    }

    /**
     * YAML转JSON
     */
    private String yamlToJson(String yaml) throws Exception {
        JsonNode yamlNode = yamlMapper.readTree(yaml);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(yamlNode);
    }

    /**
     * JSON转YAML
     */
    private String jsonToYaml(String json) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(json);
        return yamlMapper.writeValueAsString(jsonNode);
    }

    /**
     * 请求参数
     */
    @Data
    public static class ConverterRequest {
        /**
         * 操作类型：yaml-to-json、json-to-yaml
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
    public static class ConverterResponse extends BaseToolResponse {
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
