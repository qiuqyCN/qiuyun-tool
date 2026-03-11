package dev.qiuyun.qiuyuntoolbackend.executor.number;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 随机数生成工具执行器
 * 支持：整数随机数、浮点数随机数、随机密码、随机字符串、UUID等
 */
@Slf4j
@Component
public class RandomGeneratorExecutor implements ToolExecutor<RandomGeneratorExecutor.RandomGeneratorRequest, RandomGeneratorExecutor.RandomGeneratorResponse> {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String getToolCode() {
        return "random-number";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(RandomGeneratorRequest request) throws BusinessException {
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new BusinessException("生成类型不能为空");
        }

        String type = request.getType();
        if (!Arrays.asList("integer", "float", "password", "string", "uuid", "boolean", "choice").contains(type)) {
            throw new BusinessException("不支持的生成类型: " + type);
        }

        // 根据类型验证参数
        switch (type) {
            case "integer":
                if (request.getMinValue() == null || request.getMaxValue() == null) {
                    throw new BusinessException("整数随机数需要设置最小值和最大值");
                }
                if (request.getMinValue() > request.getMaxValue()) {
                    throw new BusinessException("最小值不能大于最大值");
                }
                break;
            case "float":
                if (request.getMinValue() == null || request.getMaxValue() == null) {
                    throw new BusinessException("浮点数随机数需要设置最小值和最大值");
                }
                if (request.getMinValue() > request.getMaxValue()) {
                    throw new BusinessException("最小值不能大于最大值");
                }
                if (request.getDecimalPlaces() == null || request.getDecimalPlaces() < 0 || request.getDecimalPlaces() > 10) {
                    throw new BusinessException("小数位数必须在 0-10 之间");
                }
                break;
            case "password":
                if (request.getLength() == null || request.getLength() < 1 || request.getLength() > 128) {
                    throw new BusinessException("密码长度必须在 1-128 之间");
                }
                break;
            case "string":
                if (request.getLength() == null || request.getLength() < 1 || request.getLength() > 1000) {
                    throw new BusinessException("字符串长度必须在 1-1000 之间");
                }
                break;
            case "choice":
                if (request.getChoices() == null || request.getChoices().isEmpty()) {
                    throw new BusinessException("选项列表不能为空");
                }
                break;
        }

        // 验证生成数量
        if (request.getCount() == null || request.getCount() < 1 || request.getCount() > 100) {
            throw new BusinessException("生成数量必须在 1-100 之间");
        }
    }

    @Override
    public RandomGeneratorResponse execute(RandomGeneratorRequest request, ToolContext context) throws BusinessException {
        try {
            String type = request.getType();
            int count = request.getCount();

            RandomGeneratorResponse response = new RandomGeneratorResponse();
            response.setSuccess(true);
            response.setType(type);
            response.setCount(count);

            List<String> results = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                switch (type) {
                    case "integer":
                        results.add(generateInteger(request));
                        break;
                    case "float":
                        results.add(generateFloat(request));
                        break;
                    case "password":
                        results.add(generatePassword(request));
                        break;
                    case "string":
                        results.add(generateString(request));
                        break;
                    case "uuid":
                        results.add(generateUUID());
                        break;
                    case "boolean":
                        results.add(generateBoolean());
                        break;
                    case "choice":
                        results.add(generateChoice(request));
                        break;
                }
            }

            response.setResults(results);
            return response;

        } catch (Exception e) {
            log.error("随机数生成错误: {}", e.getMessage());
            throw new BusinessException("生成失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "随机数生成",
                "description", "生成随机数、随机密码、随机字符串、UUID等",
                "types", new String[]{"integer", "float", "password", "string", "uuid", "boolean", "choice"},
                "typeLabels", Map.of(
                        "integer", "整数随机数",
                        "float", "浮点数随机数",
                        "password", "随机密码",
                        "string", "随机字符串",
                        "uuid", "UUID",
                        "boolean", "随机布尔值",
                        "choice", "随机选择"
                ),
                "maxCount", 100,
                "passwordOptions", Map.of(
                        "uppercase", "大写字母 (A-Z)",
                        "lowercase", "小写字母 (a-z)",
                        "numbers", "数字 (0-9)",
                        "symbols", "特殊符号 (!@#$%^&*)"
                )
        );
    }

    /**
     * 生成整数随机数
     */
    private String generateInteger(RandomGeneratorRequest request) {
        int min = request.getMinValue();
        int max = request.getMaxValue();
        // 生成 [min, max] 范围内的随机整数
        int randomValue = secureRandom.nextInt(max - min + 1) + min;
        return String.valueOf(randomValue);
    }

    /**
     * 生成浮点数随机数
     */
    private String generateFloat(RandomGeneratorRequest request) {
        double min = request.getMinValue();
        double max = request.getMaxValue();
        int decimalPlaces = request.getDecimalPlaces();

        // 生成 [min, max] 范围内的随机浮点数
        double randomValue = min + (max - min) * secureRandom.nextDouble();

        // 格式化小数位数
        return String.format("%." + decimalPlaces + "f", randomValue);
    }

    /**
     * 生成随机密码
     */
    private String generatePassword(RandomGeneratorRequest request) {
        int length = request.getLength();
        boolean useUppercase = request.getUseUppercase() != null ? request.getUseUppercase() : true;
        boolean useLowercase = request.getUseLowercase() != null ? request.getUseLowercase() : true;
        boolean useNumbers = request.getUseNumbers() != null ? request.getUseNumbers() : true;
        boolean useSymbols = request.getUseSymbols() != null ? request.getUseSymbols() : false;

        StringBuilder charPool = new StringBuilder();
        if (useUppercase) charPool.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (useLowercase) charPool.append("abcdefghijklmnopqrstuvwxyz");
        if (useNumbers) charPool.append("0123456789");
        if (useSymbols) charPool.append("!@#$%^&*()_+-=[]{}|;:,.<>?");

        if (charPool.length() == 0) {
            charPool.append("abcdefghijklmnopqrstuvwxyz0123456789");
        }

        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(charPool.charAt(secureRandom.nextInt(charPool.length())));
        }

        return password.toString();
    }

    /**
     * 生成随机字符串
     */
    private String generateString(RandomGeneratorRequest request) {
        int length = request.getLength();
        String charSet = request.getCharSet();

        String chars;
        if (charSet == null || charSet.isEmpty()) {
            chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        } else {
            switch (charSet.toLowerCase()) {
                case "alpha":
                    chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
                    break;
                case "alphanumeric":
                    chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                    break;
                case "numeric":
                    chars = "0123456789";
                    break;
                case "hex":
                    chars = "0123456789abcdef";
                    break;
                default:
                    chars = charSet;
            }
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }

        return result.toString();
    }

    /**
     * 生成UUID
     */
    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成随机布尔值
     */
    private String generateBoolean() {
        return secureRandom.nextBoolean() ? "true" : "false";
    }

    /**
     * 从选项中随机选择
     */
    private String generateChoice(RandomGeneratorRequest request) {
        List<String> choices = request.getChoices();
        return choices.get(secureRandom.nextInt(choices.size()));
    }

    /**
     * 请求参数
     */
    @Data
    public static class RandomGeneratorRequest {
        /**
         * 生成类型：integer、float、password、string、uuid、boolean、choice
         */
        private String type;
        /**
         * 生成数量
         */
        private Integer count = 1;
        /**
         * 最小值（整数/浮点数）
         */
        private Integer minValue;
        /**
         * 最大值（整数/浮点数）
         */
        private Integer maxValue;
        /**
         * 小数位数（浮点数）
         */
        private Integer decimalPlaces = 2;
        /**
         * 长度（密码/字符串）
         */
        private Integer length;
        /**
         * 是否使用大写字母（密码）
         */
        private Boolean useUppercase = true;
        /**
         * 是否使用小写字母（密码）
         */
        private Boolean useLowercase = true;
        /**
         * 是否使用数字（密码）
         */
        private Boolean useNumbers = true;
        /**
         * 是否使用特殊符号（密码）
         */
        private Boolean useSymbols = false;
        /**
         * 字符集（字符串）：alpha、alphanumeric、numeric、hex 或自定义
         */
        private String charSet;
        /**
         * 选项列表（随机选择）
         */
        private List<String> choices;
    }

    /**
     * 响应结果
     */
    @Data
    public static class RandomGeneratorResponse {
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 生成类型
         */
        private String type;
        /**
         * 生成数量
         */
        private int count;
        /**
         * 生成结果列表
         */
        private List<String> results;
        /**
         * 错误信息
         */
        private String errorMessage;
    }
}
