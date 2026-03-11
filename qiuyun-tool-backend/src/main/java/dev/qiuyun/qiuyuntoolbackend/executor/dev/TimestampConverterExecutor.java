package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 时间戳转换工具执行器
 * 支持：时间戳转日期、日期转时间戳、批量转换、多种格式
 */
@Slf4j
@Component
public class TimestampConverterExecutor implements ToolExecutor<TimestampConverterExecutor.TimestampRequest, TimestampConverterExecutor.TimestampResponse> {

    @Override
    public String getToolCode() {
        return "timestamp-converter";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(TimestampRequest request) throws BusinessException {
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getOperation() == null || request.getOperation().trim().isEmpty()) {
            throw new BusinessException("操作类型不能为空");
        }
        if (!"timestamp-to-date".equals(request.getOperation()) && !"date-to-timestamp".equals(request.getOperation())) {
            throw new BusinessException("不支持的操作类型: " + request.getOperation());
        }
    }

    @Override
    public TimestampResponse execute(TimestampRequest request, ToolContext context) throws BusinessException {
        try {
            String operation = request.getOperation();
            TimestampResponse response = new TimestampResponse();
            response.setSuccess(true);
            response.setOperation(operation);

            if ("timestamp-to-date".equals(operation)) {
                // 时间戳转日期
                if (request.getTimestamp() == null && (request.getTimestamps() == null || request.getTimestamps().isEmpty())) {
                    throw new BusinessException("时间戳不能为空");
                }
                
                List<ConversionResult> results = new ArrayList<>();
                
                // 处理单个时间戳
                if (request.getTimestamp() != null) {
                    results.add(convertTimestampToDate(request.getTimestamp(), request.getUnit()));
                }
                
                // 处理批量时间戳
                if (request.getTimestamps() != null) {
                    for (Long ts : request.getTimestamps()) {
                        results.add(convertTimestampToDate(ts, request.getUnit()));
                    }
                }
                
                response.setResults(results);
                
            } else if ("date-to-timestamp".equals(operation)) {
                // 日期转时间戳
                if (request.getDateString() == null || request.getDateString().trim().isEmpty()) {
                    throw new BusinessException("日期字符串不能为空");
                }
                
                ConversionResult result = convertDateToTimestamp(
                    request.getDateString(), 
                    request.getDateFormat(),
                    request.getUnit()
                );
                response.setResults(List.of(result));
            }

            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("时间戳转换错误: {}", e.getMessage());
            throw new BusinessException("转换失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getToolConfig() {
        Map<String, String> dateFormats = new HashMap<>();
        dateFormats.put("yyyy-MM-dd HH:mm:ss", "标准格式");
        dateFormats.put("yyyy-MM-dd", "日期格式");
        dateFormats.put("yyyy/MM/dd HH:mm:ss", "斜杠格式");
        dateFormats.put("dd/MM/yyyy HH:mm:ss", "欧洲格式");
        dateFormats.put("MM-dd-yyyy HH:mm:ss", "美国格式");
        dateFormats.put("yyyy年MM月dd日 HH:mm:ss", "中文格式");
        dateFormats.put("EEE, dd MMM yyyy HH:mm:ss z", "RFC格式");
        dateFormats.put("ISO", "ISO 8601");

        return Map.of(
            "name", "时间戳转换",
            "description", "Unix时间戳与日期时间互转",
            "operations", new String[]{"timestamp-to-date", "date-to-timestamp"},
            "operationLabels", Map.of(
                "timestamp-to-date", "时间戳 → 日期",
                "date-to-timestamp", "日期 → 时间戳"
            ),
            "units", new String[]{"seconds", "milliseconds"},
            "unitLabels", Map.of(
                "seconds", "秒 (s)",
                "milliseconds", "毫秒 (ms)"
            ),
            "dateFormats", dateFormats,
            "timezones", List.of("UTC", "Asia/Shanghai", "Asia/Tokyo", "America/New_York", "Europe/London")
        );
    }

    /**
     * 时间戳转日期
     */
    private ConversionResult convertTimestampToDate(Long timestamp, String unit) {
        ConversionResult result = new ConversionResult();
        result.setInput(String.valueOf(timestamp));
        
        try {
            Instant instant;
            if ("milliseconds".equals(unit)) {
                instant = Instant.ofEpochMilli(timestamp);
                result.setTimestampMs(timestamp);
                result.setTimestampSec(timestamp / 1000);
            } else {
                instant = Instant.ofEpochSecond(timestamp);
                result.setTimestampSec(timestamp);
                result.setTimestampMs(timestamp * 1000);
            }
            
            // 各种时区的时间
            Map<String, String> dateTimes = new HashMap<>();
            
            // UTC
            ZonedDateTime utc = instant.atZone(ZoneId.of("UTC"));
            dateTimes.put("UTC", formatDateTime(utc));
            
            // 本地时区
            ZonedDateTime local = instant.atZone(ZoneId.systemDefault());
            dateTimes.put("Local", formatDateTime(local));
            
            // 北京时间
            ZonedDateTime beijing = instant.atZone(ZoneId.of("Asia/Shanghai"));
            dateTimes.put("Asia/Shanghai", formatDateTime(beijing));
            
            // 东京
            ZonedDateTime tokyo = instant.atZone(ZoneId.of("Asia/Tokyo"));
            dateTimes.put("Asia/Tokyo", formatDateTime(tokyo));
            
            // 纽约
            ZonedDateTime newYork = instant.atZone(ZoneId.of("America/New_York"));
            dateTimes.put("America/New_York", formatDateTime(newYork));
            
            // 伦敦
            ZonedDateTime london = instant.atZone(ZoneId.of("Europe/London"));
            dateTimes.put("Europe/London", formatDateTime(london));
            
            result.setDateTimes(dateTimes);
            result.setSuccess(true);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("无效的时间戳: " + timestamp);
        }
        
        return result;
    }

    /**
     * 日期转时间戳
     */
    private ConversionResult convertDateToTimestamp(String dateString, String format, String unit) {
        ConversionResult result = new ConversionResult();
        result.setInput(dateString);
        
        try {
            LocalDateTime dateTime;
            
            if ("ISO".equals(format)) {
                // ISO 8601 格式
                dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                // 自定义格式
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                dateTime = LocalDateTime.parse(dateString, formatter);
            }
            
            // 转换为时间戳（使用系统默认时区）
            ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
            Instant instant = zonedDateTime.toInstant();
            
            long timestampSec = instant.getEpochSecond();
            long timestampMs = instant.toEpochMilli();
            
            result.setTimestampSec(timestampSec);
            result.setTimestampMs(timestampMs);
            
            if ("seconds".equals(unit)) {
                result.setOutput(String.valueOf(timestampSec));
            } else {
                result.setOutput(String.valueOf(timestampMs));
            }
            
            // 同时返回两种单位的时间戳
            Map<String, String> dateTimes = new HashMap<>();
            dateTimes.put("seconds", String.valueOf(timestampSec));
            dateTimes.put("milliseconds", String.valueOf(timestampMs));
            result.setDateTimes(dateTimes);
            
            result.setSuccess(true);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("日期格式错误: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(ZonedDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    /**
     * 请求参数
     */
    @Data
    public static class TimestampRequest {
        /**
         * 操作类型：timestamp-to-date、date-to-timestamp
         */
        private String operation;
        /**
         * 单个时间戳
         */
        private Long timestamp;
        /**
         * 批量时间戳
         */
        private List<Long> timestamps;
        /**
         * 日期字符串
         */
        private String dateString;
        /**
         * 日期格式
         */
        private String dateFormat = "yyyy-MM-dd HH:mm:ss";
        /**
         * 时间戳单位：seconds、milliseconds
         */
        private String unit = "milliseconds";
    }

    /**
     * 响应结果
     */
    @Data
    public static class TimestampResponse {
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 操作类型
         */
        private String operation;
        /**
         * 转换结果列表
         */
        private List<ConversionResult> results;
        /**
         * 错误信息
         */
        private String errorMessage;
    }

    /**
     * 单个转换结果
     */
    @Data
    public static class ConversionResult {
        /**
         * 输入值
         */
        private String input;
        /**
         * 输出值
         */
        private String output;
        /**
         * 秒级时间戳
         */
        private Long timestampSec;
        /**
         * 毫秒级时间戳
         */
        private Long timestampMs;
        /**
         * 各时区的日期时间
         */
        private Map<String, String> dateTimes;
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 错误信息
         */
        private String errorMessage;
    }
}
