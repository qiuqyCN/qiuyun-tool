package dev.qiuyun.qiuyuntoolbackend.executor.number;

import dev.qiuyun.qiuyuntoolbackend.constant.ToolConstants;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
public class RadixConverterExecutor extends AbstractToolExecutor<RadixConverterExecutor.RadixConverterRequest, RadixConverterExecutor.RadixConverterResponse> {

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
        validateNotNull(request, "请求");
        validateNotEmpty(request.getInput(), "输入数值");
        validateRange(request.getFromBase(), "源进制", ToolConstants.MIN_RADIX, ToolConstants.MAX_RADIX);
    }

    @Override
    protected RadixConverterResponse doExecute(RadixConverterRequest request, ToolContext context) throws Exception {
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
        if (request.getToBase() != null && request.getToBase() >= ToolConstants.MIN_RADIX && request.getToBase() <= ToolConstants.MAX_RADIX) {
            results.put(String.valueOf(request.getToBase()),
                decimalValue.toString(request.getToBase()).toUpperCase());
        }

        response.setResults(results);

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        if (e instanceof NumberFormatException) {
            return "无效的数值，请检查输入是否符合源进制规则";
        }
        return "转换失败: " + e.getMessage();
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
                "bases", ToolConstants.COMMON_RADIXES,
                "baseLabels", baseLabels,
                "maxBase", ToolConstants.MAX_RADIX,
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
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class RadixConverterResponse extends BaseToolResponse {
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
    }
}
