package dev.qiuyun.qiuyuntoolbackend.executor.network;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import dev.qiuyun.qiuyuntoolbackend.util.ip.IpQueryRequestQueue;
import dev.qiuyun.qiuyuntoolbackend.util.ip.TaobaoIpApiClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 淘宝IP查询执行器
 * 
 * 功能特性：
 * - 支持查询指定IP地址或当前访问者IP
 * - 集成IP查询请求队列，实现限流和队列缓冲
 * - 支持多级缓存（Caffeine本地缓存+Redis缓存）
 * - 自动重试机制（最多3次，指数退避）
 * - 使用内部类规范定义请求和响应参数
 * 
 * @author qiuyun
 */
@Slf4j
@Component
public class TaobaoIpQueryExecutor extends AbstractToolExecutor<TaobaoIpQueryExecutor.IpQueryRequest, TaobaoIpQueryExecutor.IpQueryResult> {

    @Autowired
    private IpQueryRequestQueue ipQueryRequestQueue;

    @Override
    public String getToolCode() {
        return "ip-query";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(IpQueryRequest request) throws BusinessException {
        validateNotNull(request, "请求");
    }

    /**
     * 执行IP查询
     * 
     * 流程：
     * 1. 获取要查询的IP地址，如果未指定则获取当前访问者IP
     * 2. 提交到IP查询队列，等待异步处理
     * 3. 转换API响应为工具响应格式
     * 
     * @param request IP查询请求
     * @param context 工具上下文
     * @return IP查询结果
     * @throws Exception 查询失败时抛出
     */
    @Override
    protected IpQueryResult doExecute(IpQueryRequest request, ToolContext context) throws Exception {
        String ip = request.getIp();

        if (ip == null || ip.trim().isEmpty()) {
            ip = getClientIp();
        }

        log.info("IP查询请求: ip={}", ip);

        try {
            CompletableFuture<TaobaoIpApiClient.CachedIpResponse> future = ipQueryRequestQueue.submit(ip);
            TaobaoIpApiClient.CachedIpResponse apiResponse = future.get();
            
            IpQueryResult result = new IpQueryResult();
            result.setSuccess(true);
            result.setIp(apiResponse.getIp());
            result.setCountry(apiResponse.getCountry());
            result.setRegion(apiResponse.getRegion());
            result.setCity(apiResponse.getCity());
            result.setIsp(apiResponse.getIsp());
            result.setRegionId(apiResponse.getRegionId());
            result.setCityId(apiResponse.getCityId());
            result.setCountryId(apiResponse.getCountryId());
            result.setSource(apiResponse.getSource());
            result.setQueryTime(apiResponse.getQueryTime());
            
            return result;
        } catch (Exception e) {
            log.error("IP查询执行失败", e);
            IpQueryResult errorResult = new IpQueryResult();
            errorResult.setSuccess(false);
            errorResult.setErrorMessage(e.getMessage() != null ? e.getMessage() : "查询失败");
            return errorResult;
        }
    }

    /**
     * 获取当前访问者的真实IP地址
     * 
     * 支持多种代理服务器的IP获取方式：
     * - X-Forwarded-For
     * - Proxy-Client-IP
     * - WL-Proxy-Client-IP
     * - X-Real-IP
     * 
     * @return 客户端IP地址，获取失败返回空字符串
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
        }
        return "";
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "IP查询失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "IP地址查询",
                "description", "查询IP地址的地理位置信息，包括国家、省份、城市和运营商"
        );
    }

    /**
     * IP查询请求参数
     */
    @Data
    public static class IpQueryRequest {
        private String ip;
    }

    /**
     * IP查询响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class IpQueryResult extends BaseToolResponse {
        private String ip;
        private String country;
        private String region;
        private String city;
        private String isp;
        private String regionId;
        private String cityId;
        private String countryId;
        private String source;
        private Long queryTime;
    }
}
