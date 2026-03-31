package dev.qiuyun.qiuyuntoolbackend.executor.network;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import dev.qiuyun.qiuyuntoolbackend.util.ip.CachedIpResponse;
import dev.qiuyun.qiuyuntoolbackend.util.ip.IpUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractIpQueryExecutor<R extends AbstractIpQueryExecutor.IpQueryResult> extends AbstractToolExecutor<AbstractIpQueryExecutor.IpQueryRequest, R> {

    protected static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    protected static final Pattern IPV6_PATTERN = Pattern.compile(
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

    protected void validateIpFormat(String ip) throws BusinessException {
        if (ip == null || ip.trim().isEmpty()) {
            return;
        }
        String trimmedIp = ip.trim();
        if (!isValidIpv4(trimmedIp) && !isValidIpv6(trimmedIp)) {
            throw new BusinessException("IP地址格式无效: " + ip);
        }
    }

    protected boolean isValidIpv4(String ip) {
        return IPV4_PATTERN.matcher(ip).matches();
    }

    protected boolean isValidIpv6(String ip) {
        return IPV6_PATTERN.matcher(ip).matches();
    }

    protected String prepareAndValidateIp(String ip) throws BusinessException {
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

        return ip;
    }

    protected void populateResult(IpQueryResult result, CachedIpResponse apiResponse) {
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
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "IP查询失败: " + e.getMessage();
    }

    @Data
    public static class IpQueryRequest {
        private String ip;
    }

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
