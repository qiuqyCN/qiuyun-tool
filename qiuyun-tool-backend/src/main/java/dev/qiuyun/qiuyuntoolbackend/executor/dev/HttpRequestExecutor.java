package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * HTTP 请求测试工具执行器
 * 支持：GET、POST、PUT、DELETE、PATCH、HEAD、OPTIONS 等 HTTP 方法
 * 功能：自定义请求头、请求体、查询参数、超时设置、响应分析等
 */
@Slf4j
@Component
public class HttpRequestExecutor extends AbstractToolExecutor<HttpRequestExecutor.HttpRequestRequest, HttpRequestExecutor.HttpRequestResponse> {

    private static final Set<String> VALID_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");
    private static final int DEFAULT_TIMEOUT = 30000; // 默认超时 30 秒
    private static final int MAX_TIMEOUT = 120000;    // 最大超时 120 秒
    private static final int MAX_RESPONSE_SIZE = 10 * 1024 * 1024; // 最大响应大小 10MB

    @Override
    public String getToolCode() {
        return "http-request";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(HttpRequestRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getUrl(), "请求 URL");
        validateNotEmpty(request.getMethod(), "请求方法");
        validateEnum(request.getMethod().toUpperCase(), "请求方法", VALID_METHODS);

        // 验证 URL 格式
        try {
            URI.create(request.getUrl());
        } catch (Exception e) {
            throw new BusinessException("无效的 URL 格式");
        }

        // 验证超时时间
        if (request.getTimeout() != null) {
            validateRange(request.getTimeout(), "超时时间", 1000, MAX_TIMEOUT);
        }

        // 验证请求头
        if (request.getHeaders() != null) {
            for (Header header : request.getHeaders()) {
                if (header.getKey() == null || header.getKey().trim().isEmpty()) {
                    throw new BusinessException("请求头名称不能为空");
                }
            }
        }
    }

    @Override
    protected HttpRequestResponse doExecute(HttpRequestRequest request, ToolContext context) throws Exception {
        long startTime = System.currentTimeMillis();

        // 创建 RestTemplate
        RestTemplate restTemplate = createRestTemplate(request.getTimeout());

        // 构建 URI（包含查询参数）
        URI uri = buildUri(request);

        // 创建请求头
        HttpHeaders headers = buildHeaders(request.getHeaders());

        // 创建请求体
        HttpEntity<String> httpEntity = new HttpEntity<>(request.getBody(), headers);

        // 执行请求
        ResponseEntity<byte[]> response;
        try {
            response = restTemplate.exchange(
                    uri,
                    HttpMethod.valueOf(request.getMethod().toUpperCase()),
                    httpEntity,
                    byte[].class
            );
        } catch (Exception e) {
            log.error("HTTP 请求失败: {}", e.getMessage());
            throw new BusinessException("请求失败: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 构建响应
        return buildResponse(response, duration, request);
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        if (e instanceof BusinessException) {
            return e.getMessage();
        }
        return "HTTP 请求失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "HTTP 请求测试",
                "description", "发送 HTTP 请求并查看响应，支持各种方法和自定义请求头",
                "methods", VALID_METHODS.toArray(new String[0]),
                "features", new String[]{"headers", "body", "params", "timeout", "responseAnalysis"}
        );
    }

    /**
     * 创建 RestTemplate
     */
    private RestTemplate createRestTemplate(Integer timeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMs = timeout != null ? timeout : DEFAULT_TIMEOUT;
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return new RestTemplate(factory);
    }

    /**
     * 构建 URI（包含查询参数）
     */
    private URI buildUri(HttpRequestRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(request.getUrl());

        // 添加查询参数
        if (request.getParams() != null) {
            for (Param param : request.getParams()) {
                if (param.getKey() != null && !param.getKey().trim().isEmpty()) {
                    builder.queryParam(param.getKey(), param.getValue());
                }
            }
        }

        return builder.build().toUri();
    }

    /**
     * 构建请求头
     */
    private HttpHeaders buildHeaders(List<Header> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();

        // 设置默认请求头
        httpHeaders.setAccept(List.of(MediaType.ALL));
        httpHeaders.setAcceptCharset(List.of(StandardCharsets.UTF_8));

        // 添加自定义请求头
        if (headers != null) {
            for (Header header : headers) {
                if (header.getKey() != null && !header.getKey().trim().isEmpty()) {
                    httpHeaders.add(header.getKey(), header.getValue());
                }
            }
        }

        return httpHeaders;
    }

    /**
     * 构建响应对象
     */
    private HttpRequestResponse buildResponse(ResponseEntity<byte[]> response, long duration, HttpRequestRequest request) {
        HttpRequestResponse result = new HttpRequestResponse();
        result.setSuccess(true);

        // 基本信息
        result.setStatusCode(response.getStatusCode().value());
        result.setStatusText(response.getStatusCode().toString());
        result.setDuration(duration);

        // 响应头
        Map<String, List<String>> responseHeaders = new HashMap<>();
        response.getHeaders().forEach((key, values) -> {
            responseHeaders.put(key, new ArrayList<>(values));
        });
        result.setHeaders(responseHeaders);

        // 响应体处理
        byte[] bodyBytes = response.getBody();
        if (bodyBytes != null) {
            // 限制响应大小
            if (bodyBytes.length > MAX_RESPONSE_SIZE) {
                result.setBody("[响应体过大，已截断显示。总大小: " + formatBytes(bodyBytes.length) + "]");
                result.setBodyTruncated(true);
                result.setBodySize(bodyBytes.length);
            } else {
                // 检查是否需要解压（GZIP）
                String contentEncoding = response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
                byte[] processedBytes = bodyBytes;

                if ("gzip".equalsIgnoreCase(contentEncoding)) {
                    try {
                        processedBytes = decompressGzip(bodyBytes);
                    } catch (IOException e) {
                        log.warn("GZIP 解压失败: {}", e.getMessage());
                    }
                }

                // 检测编码
                Charset detectedCharset = detectCharset(response.getHeaders(), processedBytes);
                String bodyText = new String(processedBytes, detectedCharset);
                result.setBody(bodyText);
                result.setBodyTruncated(false);
                result.setBodySize(processedBytes.length);

                // 计算哈希值
                result.setBodyHash(calculateHash(processedBytes));

                // 检测内容类型
                result.setContentType(detectContentType(response.getHeaders(), bodyText));
            }
        } else {
            result.setBody("");
            result.setBodySize(0);
        }

        // 请求信息
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setMethod(request.getMethod().toUpperCase());
        requestInfo.setUrl(request.getUrl());
        requestInfo.setFinalUrl(buildUri(request).toString());
        if (request.getHeaders() != null) {
            requestInfo.setHeaders(request.getHeaders().stream()
                    .collect(Collectors.toMap(Header::getKey, Header::getValue, (v1, v2) -> v1)));
        }
        result.setRequest(requestInfo);

        // 响应分析
        result.setAnalysis(analyzeResponse(response, duration));

        return result;
    }

    /**
     * GZIP 解压
     */
    private byte[] decompressGzip(byte[] compressed) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            return gis.readAllBytes();
        }
    }

    /**
     * 检测字符编码
     */
    private Charset detectCharset(HttpHeaders headers, byte[] bodyBytes) {
        // 从 Content-Type 获取编码
        MediaType contentType = headers.getContentType();
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        }

        // 尝试从 HTML meta 标签检测
        if (bodyBytes.length > 0) {
            String preview = new String(bodyBytes, 0, Math.min(bodyBytes.length, 1024), StandardCharsets.UTF_8);
            if (preview.toLowerCase().contains("charset=gb2312") || preview.toLowerCase().contains("charset=gbk")) {
                return Charset.forName("GBK");
            }
        }

        return StandardCharsets.UTF_8;
    }

    /**
     * 检测内容类型
     */
    private String detectContentType(HttpHeaders headers, String body) {
        MediaType contentType = headers.getContentType();
        if (contentType != null) {
            return contentType.toString();
        }

        // 根据内容推断
        String trimmed = body.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return "application/json (推断)";
        } else if (trimmed.startsWith("<?xml") || trimmed.startsWith("<")) {
            return "text/xml (推断)";
        } else if (trimmed.startsWith("<!DOCTYPE html") || trimmed.toLowerCase().contains("<html")) {
            return "text/html (推断)";
        }

        return "text/plain (推断)";
    }

    /**
     * 计算 MD5 哈希
     */
    private String calculateHash(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 格式化字节大小
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }

    /**
     * 分析响应
     */
    private ResponseAnalysis analyzeResponse(ResponseEntity<byte[]> response, long duration) {
        ResponseAnalysis analysis = new ResponseAnalysis();

        // 状态码分析
        int statusCode = response.getStatusCode().value();
        analysis.setStatusCategory(getStatusCategory(statusCode));
        analysis.setStatusDescription(getStatusDescription(statusCode));

        // 性能分析
        if (duration < 200) {
            analysis.setPerformance("极快");
        } else if (duration < 500) {
            analysis.setPerformance("快");
        } else if (duration < 1000) {
            analysis.setPerformance("正常");
        } else if (duration < 3000) {
            analysis.setPerformance("较慢");
        } else {
            analysis.setPerformance("慢");
        }

        // 缓存分析
        String cacheControl = response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL);
        if (cacheControl != null) {
            analysis.setCacheable(!cacheControl.contains("no-cache") && !cacheControl.contains("no-store"));
        } else {
            analysis.setCacheable(true);
        }

        // 压缩分析
        String contentEncoding = response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        analysis.setCompressed(contentEncoding != null && (contentEncoding.contains("gzip") || contentEncoding.contains("deflate")));

        return analysis;
    }

    /**
     * 获取状态码分类
     */
    private String getStatusCategory(int statusCode) {
        return switch (statusCode / 100) {
            case 1 -> "信息响应";
            case 2 -> "成功响应";
            case 3 -> "重定向";
            case 4 -> "客户端错误";
            case 5 -> "服务器错误";
            default -> "未知";
        };
    }

    /**
     * 获取状态码描述
     */
    private String getStatusDescription(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK - 请求成功";
            case 201 -> "Created - 已创建";
            case 204 -> "No Content - 无内容";
            case 301 -> "Moved Permanently - 永久重定向";
            case 302 -> "Found - 临时重定向";
            case 304 -> "Not Modified - 未修改";
            case 400 -> "Bad Request - 错误请求";
            case 401 -> "Unauthorized - 未授权";
            case 403 -> "Forbidden - 禁止访问";
            case 404 -> "Not Found - 未找到";
            case 405 -> "Method Not Allowed - 方法不允许";
            case 500 -> "Internal Server Error - 服务器内部错误";
            case 502 -> "Bad Gateway - 错误网关";
            case 503 -> "Service Unavailable - 服务不可用";
            default -> "未知状态码";
        };
    }

    // ==================== 请求/响应类定义 ====================

    /**
     * 请求参数
     */
    @Data
    public static class HttpRequestRequest {
        /**
         * 请求方法：GET、POST、PUT、DELETE、PATCH、HEAD、OPTIONS
         */
        private String method = "GET";

        /**
         * 请求 URL
         */
        private String url;

        /**
         * 请求头列表
         */
        private List<Header> headers;

        /**
         * 查询参数列表
         */
        private List<Param> params;

        /**
         * 请求体
         */
        private String body;

        /**
         * 超时时间（毫秒），默认 30000
         */
        private Integer timeout;

        /**
         * 是否跟随重定向
         */
        private boolean followRedirects = true;
    }

    /**
     * 请求头
     */
    @Data
    public static class Header {
        private String key;
        private String value;
    }

    /**
     * 查询参数
     */
    @Data
    public static class Param {
        private String key;
        private String value;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class HttpRequestResponse extends BaseToolResponse {
        /**
         * HTTP 状态码
         */
        private int statusCode;

        /**
         * 状态文本
         */
        private String statusText;

        /**
         * 响应头
         */
        private Map<String, List<String>> headers;

        /**
         * 响应体
         */
        private String body;

        /**
         * 响应体大小（字节）
         */
        private long bodySize;

        /**
         * 响应体是否被截断
         */
        private boolean bodyTruncated;

        /**
         * 响应体哈希（MD5）
         */
        private String bodyHash;

        /**
         * 内容类型
         */
        private String contentType;

        /**
         * 请求耗时（毫秒）
         */
        private long duration;

        /**
         * 请求信息
         */
        private RequestInfo request;

        /**
         * 响应分析
         */
        private ResponseAnalysis analysis;
    }

    /**
     * 请求信息
     */
    @Data
    public static class RequestInfo {
        private String method;
        private String url;
        private String finalUrl;
        private Map<String, String> headers;
    }

    /**
     * 响应分析
     */
    @Data
    public static class ResponseAnalysis {
        /**
         * 状态码分类
         */
        private String statusCategory;

        /**
         * 状态码描述
         */
        private String statusDescription;

        /**
         * 性能评估
         */
        private String performance;

        /**
         * 是否可缓存
         */
        private boolean cacheable;

        /**
         * 是否压缩
         */
        private boolean compressed;
    }
}
