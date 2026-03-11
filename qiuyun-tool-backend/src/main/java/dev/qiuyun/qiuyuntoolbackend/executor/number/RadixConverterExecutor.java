package dev.qiuyun.qiuyuntoolbackend.executor.number;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 进制转换工具执行器
 * 支持：二进制、八进制、十进制、十六进制互转，以及任意进制转换
 */
@Slf4j
@Component
public class RadixConverterExecutor implements ToolExecutor<RadixConverterExecutor.RadixConverterRequest, RadixConverterExecutor.RadixConverterResponse> {

    @Override
    public String getToolCode() {
        return "radix-converter";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(RadixConverterRequest request) throws BusinessException {
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getInput() == null || request.getInput().trim().isEmpty()) {
            throw new BusinessException("输入数值不能为空");
        }
        if (request.getFromBase() < 2 || request.getFromBase() > 36) {
            throw new BusinessException("源进制必须在 2-36 之间");
        }
    }

    @Override
    public RadixConverterResponse execute(RadixConverterRequest request, ToolContext context) throws BusinessException {
        try {
            String input = request.getInput().trim().toUpperCase();
            int fromBase = request.getFromBase();

            // 验证输入是否合法
            validateInput(input, fromBase);

            // 转换为十进制（中间值）
            BigInteger decimalValue = new BigInteger(input, fromBase);

            RadixConverterResponse response = new RadixConverterResponse();
            response.setSuccess(true);
            response.setInput(input);
            response.setFromBase(fromBase);
            response.setDecimalValue(decimalValue.toString());

            // 转换为各种进制
            Map<String, String> results = new HashMap<>();
            results.put("2", decimalValue.toString(2));   // 二进制
            results.put("8", decimalValue.toString(8));   // 八进制
            results.put("10", decimalValue.toString(10)); // 十进制
            results.put("16", decimalValue.toString(16).toUpperCase()); // 十六进制

            // 如果有目标进制，也进行转换
            if (request.getToBase() != null && request.getToBase() >= 2 && request.getToBase() <= 36) {
                results.put(String.valueOf(request.getToBase()), 
                    decimalValue.toString(request.getToBase()).toUpperCase());
            }

            response.setResults(results);

            return response;

        } catch (NumberFormatException e) {
            log.error("进制转换错误: 无效的数值 {}", request.getInput());
            throw new BusinessException("无效的数值: " + request.getInput() + "，请检查输入是否符合源进制规则");
        } catch (Exception e) {
            log.error("进制转换错误: {}", e.getMessage());
            throw new BusinessException("转换失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getToolConfig() {
        Map<String, String> baseLabels = new HashMap<>();
        baseLabels.put("2", "二进制 (Binary)");
        baseLabels.put("8", "八进制 (Octal)");
        baseLabels.put("10", "十进制 (Decimal)");
        baseLabels.put("16", "十六进制 (Hexadecimal)");

        return Map.of(
                "name", "进制转换",
                "description", "二进制、八进制、十进制、十六进制互转",
                "bases", new Integer[]{2, 8, 10, 16},
                "baseLabels", baseLabels,
                "maxBase", 36,
                "supportedChars", "0-9, A-Z"
        );
    }

    /**
     * 验证输入是否合法
     */
    private void validateInput(String input, int base) throws BusinessException {
        String validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(0, base);
        
        for (char c : input.toCharArray()) {
            if (validChars.indexOf(c) < 0) {
                throw new BusinessException(
                    String.format("字符 '%c' 在 %d 进制中不合法", c, base)
                );
            }
        }
    }

    /**
     * 请求参数
     */
    @Data
    public static class RadixConverterRequest {
        /**
         * 输入数值
         */
        private String input;
        /**
         * 源进制 (2-36)
         */
        private int fromBase = 10;
        /**
         * 目标进制 (可选，2-36)
         */
        private Integer toBase;
    }

    /**
     * 响应结果
     */
    @Data
    public static class RadixConverterResponse {
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 输入数值
         */
        private String input;
        /**
         * 源进制
         */
        private int fromBase;
        /**
         * 十进制值
         */
        private String decimalValue;
        /**
         * 各进制结果
         */
        private Map<String, String> results;
        /**
         * 错误信息
         */
        private String errorMessage;
    }
}
