package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON/Java 互转工具执行器
 * 支持：JSON 转 Java POJO 类，Java 对象转 JSON
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonJavaConverterExecutor extends AbstractToolExecutor<JsonJavaConverterExecutor.ConverterRequest, JsonJavaConverterExecutor.ConverterResponse> {

    private final ObjectMapper objectMapper;

    @Override
    public String getToolCode() {
        return "json-java-converter";
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
        String input = request.getInput().trim();
        String operation = request.getOperation();
        String packageName = request.getPackageName();
        boolean useLombok = request.isUseLombok();
        String rootClassName = request.getRootClassName();

        String result;
        if ("json-to-java".equals(operation)) {
            result = jsonToJava(input, packageName, useLombok, rootClassName);
        } else if ("java-to-json".equals(operation)) {
            result = javaToJson(input);
        } else {
            throw new BusinessException("不支持的操作类型: " + operation);
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
                "name", "JSON/Java 互转",
                "description", "JSON 与 Java POJO 类互相转换",
                "operations", new String[]{"json-to-java", "java-to-json"},
                "operationLabels", Map.of(
                        "json-to-java", "JSON → Java",
                        "java-to-json", "Java → JSON"
                ),
                "features", new String[]{"lombok", "packageName", "rootClassName"}
        );
    }

    /**
     * JSON 转 Java POJO
     */
    private String jsonToJava(String json, String packageName, boolean useLombok, String rootClassName) throws Exception {
        JsonNode rootNode = objectMapper.readTree(json);

        if (!rootNode.isObject()) {
            throw new BusinessException("JSON 根元素必须是对象");
        }

        StringBuilder sb = new StringBuilder();

        // 包声明
        if (packageName != null && !packageName.isEmpty()) {
            sb.append("package ").append(packageName).append(";\n\n");
        }

        // 导入语句
        if (useLombok) {
            sb.append("import lombok.Data;\n");
            sb.append("import lombok.NoArgsConstructor;\n");
            sb.append("import lombok.AllArgsConstructor;\n");
        }
        sb.append("import java.util.List;\n");
        sb.append("import java.util.Date;\n");
        sb.append("import com.fasterxml.jackson.annotation.JsonProperty;\n");
        sb.append("\n");

        // 生成主类
        String className = (rootClassName != null && !rootClassName.isEmpty())
                ? rootClassName
                : "Root";
        generateClass(sb, className, (ObjectNode) rootNode, useLombok, 0);

        return sb.toString().trim();
    }

    /**
     * 生成 Java 类
     */
    private void generateClass(StringBuilder sb, String className, ObjectNode node, boolean useLombok, int indent) {
        String indentStr = "    ".repeat(indent);

        // Lombok 注解
        if (useLombok) {
            sb.append(indentStr).append("@Data\n");
            sb.append(indentStr).append("@NoArgsConstructor\n");
            sb.append(indentStr).append("@AllArgsConstructor\n");
        }

        sb.append(indentStr).append("public class ").append(className).append(" {\n");

        // 收集内部类
        Map<String, ObjectNode> innerClasses = new LinkedHashMap<>();

        Set<Map.Entry<String, JsonNode>> fields = node.properties();
        for (Map.Entry<String, JsonNode> entry : fields) {
            String fieldName = entry.getKey();
            JsonNode fieldValue = entry.getValue();

            String javaType = inferJavaType(fieldName, fieldValue, innerClasses);
            String javaFieldName = toCamelCase(fieldName);

            sb.append(indentStr).append("    @JsonProperty(\"").append(fieldName).append("\")\n");
            sb.append(indentStr).append("    private ").append(javaType).append(" ").append(javaFieldName).append(";\n");
            sb.append("\n");
        }

        // Getter/Setter（如果不使用 Lombok）
        if (!useLombok) {
            fields = node.properties();
            for (Map.Entry<String, JsonNode> entry : fields) {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                String javaType = inferJavaType(fieldName, fieldValue, new LinkedHashMap<>());
                String javaFieldName = toCamelCase(fieldName);
                String capitalizedName = capitalize(javaFieldName);

                // Getter
                sb.append(indentStr).append("    public ").append(javaType).append(" get").append(capitalizedName).append("() {\n");
                sb.append(indentStr).append("        return ").append(javaFieldName).append(";\n");
                sb.append(indentStr).append("    }\n\n");

                // Setter
                sb.append(indentStr).append("    public void set").append(capitalizedName).append("(").append(javaType).append(" ").append(javaFieldName).append(") {\n");
                sb.append(indentStr).append("        this.").append(javaFieldName).append(" = ").append(javaFieldName).append(";\n");
                sb.append(indentStr).append("    }\n\n");
            }
        }

        sb.append(indentStr).append("}\n\n");

        // 生成内部类
        for (Map.Entry<String, ObjectNode> innerClass : innerClasses.entrySet()) {
            generateClass(sb, innerClass.getKey(), innerClass.getValue(), useLombok, indent);
        }
    }

    /**
     * 推断 Java 类型
     */
    private String inferJavaType(String fieldName, JsonNode node, Map<String, ObjectNode> innerClasses) {
        if (node.isTextual()) {
            return "String";
        } else if (node.isInt()) {
            return "Integer";
        } else if (node.isLong()) {
            return "Long";
        } else if (node.isDouble() || node.isFloat()) {
            return "Double";
        } else if (node.isBoolean()) {
            return "Boolean";
        } else if (node.isObject()) {
            String className = capitalize(toCamelCase(fieldName));
            innerClasses.put(className, (ObjectNode) node);
            return className;
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            if (!arrayNode.isEmpty()) {
                JsonNode firstElement = arrayNode.get(0);
                String elementType = inferJavaType(fieldName, firstElement, innerClasses);
                return "List<" + elementType + ">";
            }
            return "List<Object>";
        } else if (node.isNull()) {
            return "Object";
        }
        return "Object";
    }

    /**
     * Java 类转 JSON
     * 解析 Java 类定义，生成示例 JSON 对象或 JSON Schema
     */
    private String javaToJson(String javaInput) throws Exception {
        // 先尝试解析为 JSON（如果用户直接输入了 JSON）
        try {
            JsonNode node = objectMapper.readTree(javaInput);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception ignored) {
            // 不是 JSON，继续尝试解析 Java 类
        }

        // 解析 Java 类定义
        JavaClassInfo classInfo = parseJavaClass(javaInput);

        // 生成示例 JSON 对象
        Map<String, Object> example = generateExampleJson(classInfo);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(example);
    }

    /**
     * Java 类信息
     */
    @Data
    private static class JavaClassInfo {
        private String className;
        private String packageName;
        private List<FieldInfo> fields = new ArrayList<>();
    }

    /**
     * 字段信息
     */
    @Data
    @AllArgsConstructor
    public static class FieldInfo {
        private String type;
        private String name;
    }

    /**
     * 解析 Java 类定义
     */
    private JavaClassInfo parseJavaClass(String javaCode) throws Exception {
        JavaClassInfo classInfo = new JavaClassInfo();

        // 移除注释
        javaCode = removeComments(javaCode);

        // 提取包名
        Pattern packagePattern = Pattern.compile("package\\s+([\\w.]+);");
        Matcher packageMatcher = packagePattern.matcher(javaCode);
        if (packageMatcher.find()) {
            classInfo.setPackageName(packageMatcher.group(1));
        }

        // 提取类名
        Pattern classPattern = Pattern.compile("(?:public\\s+)?(?:class|record)\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(javaCode);
        if (classMatcher.find()) {
            classInfo.setClassName(classMatcher.group(1));
        } else {
            throw new BusinessException("无法找到类定义");
        }

        // 提取字段（支持多种格式）
        // 1. 标准字段格式：private String name;
        Pattern fieldPattern = Pattern.compile("(?:private|public|protected)\\s+(?:static\\s+)?(?:final\\s+)?([\\w<>,\\s]+?)\\s+(\\w+)\\s*[=;{]");
        Matcher fieldMatcher = fieldPattern.matcher(javaCode);
        while (fieldMatcher.find()) {
            String type = fieldMatcher.group(1).trim();
            String name = fieldMatcher.group(2).trim();
            classInfo.getFields().add(new FieldInfo(type, name));
        }

        // 2. Record 格式：ClassName(Type1 field1, Type2 field2)
        if (javaCode.contains("record " + classInfo.getClassName())) {
            Pattern recordPattern = Pattern.compile("record\\s+" + classInfo.getClassName() + "\\s*\\(([^)]+)\\)");
            Matcher recordMatcher = recordPattern.matcher(javaCode);
            if (recordMatcher.find()) {
                String params = recordMatcher.group(1);
                String[] paramList = params.split(",");
                for (String param : paramList) {
                    param = param.trim();
                    String[] parts = param.split("\\s+");
                    if (parts.length >= 2) {
                        String type = parts[parts.length - 2];
                        String name = parts[parts.length - 1];
                        classInfo.getFields().add(new FieldInfo(type, name));
                    }
                }
            }
        }

        // 3. Lombok @Data 类的字段（可能没有 getter/setter，只有字段声明）
        if (classInfo.getFields().isEmpty()) {
            Pattern simpleFieldPattern = Pattern.compile("(?:private|public|protected)\\s+([\\w<>,\\s]+?)\\s+(\\w+)\\s*;");
            Matcher simpleFieldMatcher = simpleFieldPattern.matcher(javaCode);
            while (simpleFieldMatcher.find()) {
                String type = simpleFieldMatcher.group(1).trim();
                String name = simpleFieldMatcher.group(2).trim();
                classInfo.getFields().add(new FieldInfo(type, name));
            }
        }

        return classInfo;
    }

    /**
     * 移除 Java 代码中的注释
     */
    private String removeComments(String code) {
        // 移除单行注释
        code = code.replaceAll("//[^\\n]*", "");
        // 移除多行注释
        code = code.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        // 移除 Javadoc
        code = code.replaceAll("/\\*\\*[\\s\\S]*?\\*/", "");
        return code;
    }

    /**
     * 生成示例 JSON 对象
     */
    private Map<String, Object> generateExampleJson(JavaClassInfo classInfo) {
        Map<String, Object> example = new LinkedHashMap<>();

        for (FieldInfo field : classInfo.getFields()) {
            example.put(field.getName(), generateExampleValue(field.getType()));
        }

        return example;
    }

    /**
     * 根据类型生成示例值
     */
    private Object generateExampleValue(String type) {
        type = type.trim();

        // 基本类型
        switch (type) {
            case "String":
            case "java.lang.String":
                return "示例字符串";
            case "int":
            case "Integer":
            case "java.lang.Integer":
                return 0;
            case "long":
            case "Long":
            case "java.lang.Long":
                return 0L;
            case "double":
            case "Double":
            case "java.lang.Double":
                return 0.0;
            case "float":
            case "Float":
            case "java.lang.Float":
                return 0.0f;
            case "boolean":
            case "Boolean":
            case "java.lang.Boolean":
                return true;
            case "byte":
            case "Byte":
                return (byte) 0;
            case "short":
            case "Short":
                return (short) 0;
            case "char":
            case "Character":
            case "java.lang.Character":
                return 'A';
            case "Date":
            case "java.util.Date":
            case "LocalDate":
            case "java.time.LocalDate":
                return "2024-01-01";
            case "LocalDateTime":
            case "java.time.LocalDateTime":
                return "2024-01-01T00:00:00";
            default:
                // 处理泛型集合
                if (type.startsWith("List<") || type.startsWith("ArrayList<") || type.startsWith("java.util.List<")) {
                    String genericType = extractGenericType(type);
                    List<Object> list = new ArrayList<>();
                    list.add(generateExampleValue(genericType));
                    return list;
                }
                if (type.startsWith("Set<") || type.startsWith("HashSet<") || type.startsWith("java.util.Set<")) {
                    String genericType = extractGenericType(type);
                    List<Object> list = new ArrayList<>();
                    list.add(generateExampleValue(genericType));
                    return list;
                }
                if (type.startsWith("Map<") || type.startsWith("HashMap<") || type.startsWith("java.util.Map<")) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("key", "value");
                    return map;
                }
                // 其他对象类型
                return new LinkedHashMap<>();
        }
    }

    /**
     * 提取泛型类型
     */
    private String extractGenericType(String type) {
        int start = type.indexOf('<');
        int end = type.lastIndexOf('>');
        if (start > 0 && end > start) {
            return type.substring(start + 1, end).trim();
        }
        return "Object";
    }

    /**
     * 转换为驼峰命名
     */
    private String toCamelCase(String input) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpperCase = false;

        for (char c : input.toCharArray()) {
            if (c == '_' || c == '-') {
                nextUpperCase = true;
            } else if (nextUpperCase) {
                sb.append(Character.toUpperCase(c));
                nextUpperCase = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * 首字母大写
     */
    private String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    /**
     * 请求参数
     */
    @Data
    public static class ConverterRequest {
        /**
         * 操作类型：json-to-java、java-to-json
         */
        private String operation;
        /**
         * 输入内容
         */
        private String input;
        /**
         * 包名
         */
        private String packageName = "com.example";
        /**
         * 是否使用 Lombok
         */
        private boolean useLombok = true;
        /**
         * 根类名
         */
        private String rootClassName = "Root";
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
