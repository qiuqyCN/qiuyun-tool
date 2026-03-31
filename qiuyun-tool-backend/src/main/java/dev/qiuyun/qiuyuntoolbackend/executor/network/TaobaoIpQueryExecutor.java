package dev.qiuyun.qiuyuntoolbackend.executor.network;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import dev.qiuyun.qiuyuntoolbackend.util.ip.IpQueryRequestQueue;
import dev.qiuyun.qiuyuntoolbackend.util.ip.IpUtil;
import dev.qiuyun.qiuyuntoolbackend.util.ip.TaobaoIpApiClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

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

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
            "^([0-9a-fA-F]{1,4}:){1,7}:$|" +
            "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|" +
            "^([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}$|" +
            "^([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}$|" +
            "^([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}$|" +
            "^([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}$|" +
            "^[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})$|" +
            "^:((:[0-9a-fA-F]{1,4}){1,7}|:)$|" +
            "^fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}$|" +
            "^::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])$|" +
            "^([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])$"
    );

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
        if (request.getIp() != null && !request.getIp().trim().isEmpty()) {
            validateIpFormat(request.getIp());
        }
    }

    /**
     * 验证IP地址格式
     *
     * @param ip IP地址
     * @throws BusinessException 如果IP格式无效
     */
    private void validateIpFormat(String ip) throws BusinessException {
        if (ip == null || ip.trim().isEmpty()) {
            return;
        }
        String trimmedIp = ip.trim();
        if (!isValidIpv4(trimmedIp) && !isValidIpv6(trimmedIp)) {
            throw new BusinessException("IP地址格式无效: " + ip);
        }
    }

    /**
     * 验证是否是有效的IPv4地址
     *
     * @param ip IP地址
     * @return true表示有效，false表示无效
     */
    private boolean isValidIpv4(String ip) {
        return IPV4_PATTERN.matcher(ip).matches();
    }

    /**
     * 验证是否是有效的IPv6地址
     *
     * @param ip IP地址
     * @return true表示有效，false表示无效
     */
    private boolean isValidIpv6(String ip) {
        return IPV6_PATTERN.matcher(ip).matches();
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
            ip = IpUtil.getClientIp();
        }

        if (ip == null || ip.trim().isEmpty()) {
            throw new BusinessException("无法获取客户端IP地址，请手动输入IP地址进行查询");
        }

        ip = ip.trim();
        
        if (IpUtil.isInternalOrLocalIp(ip)) {
            throw new BusinessException("当前为内网/本地IP地址（" + ip + "），请输入具体的公网IP地址进行查询");
        }
        
        log.info("IP查询请求: ip={}", ip);

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
