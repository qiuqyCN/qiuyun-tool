package dev.qiuyun.qiuyuntoolbackend.executor.dev;

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
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * YAML 与 Properties 互转工具执行器
 * 支持：YAML 格式与 Java Properties 文件格式双向转换
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YamlPropertiesConverterExecutor extends AbstractToolExecutor<YamlPropertiesConverterExecutor.ConverterRequest, YamlPropertiesConverterExecutor.ConverterResponse> {

    private final ObjectMapper objectMapper;
    private final YAMLMapper yamlMapper;

    @Override
    public String getToolCode() {
        return "yaml-properties-converter";
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
    }

    @Override
    protected ConverterResponse doExecute(ConverterRequest request, ToolContext context) throws Exception {
        String input = request.getInput();
        String operation = request.getOperation();
        boolean sortKeys = request.isSortKeys();

        String output;
        int itemCount;

        if ("yaml-to-properties".equals(operation)) {
            // YAML 转 Properties
            Map<String, Object> yamlMap = parseYaml(input);
            Map<String, String> flatMap = flattenMap(yamlMap, "");
            if (sortKeys) {
                flatMap = new TreeMap<>(flatMap);
            }
            output = convertToProperties(flatMap);
            itemCount = flatMap.size();
        } else if ("properties-to-yaml".equals(operation)) {
            // Properties 转 YAML
            Map<String, Object> propertiesMap = parseProperties(input, sortKeys);
            Map<String, Object> nestedMap = buildNestedMap(propertiesMap);
            output = convertToYaml(nestedMap);
            itemCount = propertiesMap.size();
        } else {
            throw new BusinessException("不支持的操作类型: " + operation);
        }

        ConverterResponse response = new ConverterResponse();
        response.setSuccess(true);
        response.setInput(input);
        response.setOutput(output);
        response.setOperation(operation);
        response.setItemCount(itemCount);
        response.setLineCount(input.split("\r?\n").length);

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        if (e instanceof IOException) {
            return "格式错误: " + e.getMessage();
        }
        return "转换失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "YAML/Properties 互转",
                "description", "YAML 格式与 Java Properties 配置文件双向转换",
                "operations", new String[]{"yaml-to-properties", "properties-to-yaml"},
                "operationLabels", Map.of(
                        "yaml-to-properties", "YAML → Properties",
                        "properties-to-yaml", "Properties → YAML"
                ),
                "features", new String[]{"sortKeys"}
        );
    }

    // ==================== YAML 转 Properties ====================

    /**
     * 解析 YAML 字符串
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYaml(String input) {
        Yaml yaml = new Yaml();
        Object result = yaml.load(input);

        if (result == null) {
            return new LinkedHashMap<>();
        }

        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }

        throw new BusinessException("YAML 根元素必须是对象（Map）");
    }

    /**
     * 将嵌套 Map 扁平化
     */
    private Map<String, String> flattenMap(Map<String, Object> map, String prefix) {
        Map<String, String> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                result.putAll(flattenMap(nestedMap, fullKey));
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) value;
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    String listKey = fullKey + "[" + i + "]";
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        result.putAll(flattenMap(itemMap, listKey));
                    } else {
                        result.put(listKey, formatValue(item));
                    }
                }
            } else {
                result.put(fullKey, formatValue(value));
            }
        }

        return result;
    }

    /**
     * 格式化值为字符串
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    /**
     * 转换为 Properties 字符串
     */
    private String convertToProperties(Map<String, String> flatMap) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : flatMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // 转义特殊字符
            String escapedValue = escapePropertiesValue(value);
            sb.append(key).append("=").append(escapedValue).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * 转义 Properties 值中的特殊字符
     */
    private String escapePropertiesValue(String value) {
        if (value == null) {
            return "";
        }

        // 转义反斜杠、换行符、回车符、制表符
        return value
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ==================== Properties 转 YAML ====================

    /**
     * 解析 Properties 字符串
     */
    private Map<String, Object> parseProperties(String input, boolean sortKeys) {
        Properties props = new Properties();

        try {
            props.load(new StringReader(input));
        } catch (IOException e) {
            throw new BusinessException("解析 Properties 失败: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();

        List<String> keys = props.stringPropertyNames().stream()
                .collect(Collectors.toList());

        if (sortKeys) {
            Collections.sort(keys);
        }

        for (String key : keys) {
            String value = props.getProperty(key);
            result.put(key, convertValue(value));
        }

        return result;
    }

    /**
     * 转换值类型（尝试解析为数字或布尔值）
     */
    private Object convertValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        String trimmed = value.trim();

        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
        }

        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ignored) {
        }

        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException ignored) {
        }

        if (trimmed.equalsIgnoreCase("true")) {
            return true;
        }
        if (trimmed.equalsIgnoreCase("false")) {
            return false;
        }

        return value;
    }

    /**
     * 构建嵌套 Map 结构
     */
    private Map<String, Object> buildNestedMap(Map<String, Object> flatMap) {
        Map<String, Object> root = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String[] parts = key.split("\\.");

            Map<String, Object> current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];

                if (part.matches(".+\\[\\d+\\]$")) {
                    String arrayName = part.substring(0, part.indexOf('['));
                    int index = Integer.parseInt(part.substring(part.indexOf('[') + 1, part.indexOf(']')));

                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) current.computeIfAbsent(arrayName, k -> new ArrayList<>());

                    while (list.size() <= index) {
                        list.add(new LinkedHashMap<>());
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> next = (Map<String, Object>) list.get(index);
                    current = next;
                } else {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> next = (Map<String, Object>) current.computeIfAbsent(part, k -> new LinkedHashMap<>());
                    current = next;
                }
            }

            String lastPart = parts[parts.length - 1];
            if (lastPart.matches(".+\\[\\d+\\]$")) {
                String arrayName = lastPart.substring(0, lastPart.indexOf('['));
                int index = Integer.parseInt(lastPart.substring(lastPart.indexOf('[') + 1, lastPart.indexOf(']')));

                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) current.computeIfAbsent(arrayName, k -> new ArrayList<>());

                while (list.size() <= index) {
                    list.add(null);
                }
                list.set(index, value);
            } else {
                current.put(lastPart, value);
            }
        }

        return root;
    }

    /**
     * 转换为 YAML 字符串
     */
    private String convertToYaml(Map<String, Object> map) throws IOException {
        StringWriter writer = new StringWriter();
        yamlMapper.writeValue(writer, map);
        return writer.toString().trim();
    }

    /**
     * 请求参数
     */
    @Data
    public static class ConverterRequest {
        /**
         * 操作类型：yaml-to-properties、properties-to-yaml
         */
        private String operation;
        /**
         * 输入内容
         */
        private String input;
        /**
         * 是否对键进行排序，默认 false
         */
        private boolean sortKeys = false;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ConverterResponse extends BaseToolResponse {
        /**
         * 输入内容
         */
        private String input;
        /**
         * 输出内容
         */
        private String output;
        /**
         * 操作类型
         */
        private String operation;
        /**
         * 键值对数量
         */
        private int itemCount;
        /**
         * 输入行数
         */
        private int lineCount;
    }
}
