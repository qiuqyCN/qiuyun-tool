package dev.qiuyun.qiuyuntoolbackend.util.ip;

import com.google.common.util.concurrent.RateLimiter;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.model.dto.PconlineIpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


/**
 * 太平洋在线IP客户端
 * 请求示例：
 * https://whois.pconline.com.cn/ipJson.jsp?ip=218.201.25.123&json=true
 *
 * 成功响应示例：
 * {"ip":"218.201.25.123","pro":"重庆市","proCode":"500000","city":"重庆市","cityCode":"500000","region":"","regionCode":"0","addr":"重庆市 移动","regionNames":"","err":""}
 *
 * 失败响应示例：
 * 503
 */
@Slf4j
@Component
public class PconlineIpApiClient extends AbstractIpApiClient {

    private static final String CACHE_PREFIX = "ip:pconline:";
    private static final double PERMITS_PER_SECOND = 1.0;

    public PconlineIpApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://whois.pconline.com.cn")
                .build();
        this.rateLimiter = RateLimiter.create(PERMITS_PER_SECOND);
        initCache();
    }

    @Override
    protected String getCachePrefix() {
        return CACHE_PREFIX;
    }

    @Override
    protected boolean useRateLimiter() {
        return true;
    }

    @Override
    protected CachedIpResponse doQueryFromApi(String ip) {
        acquireRateLimiter();
        log.debug("调用太平洋在线API: ip={}", ip);

        String jsonResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ipJson.jsp")
                        .queryParam("ip", ip)
                        .queryParam("json", "true")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new BusinessException("API调用失败，状态码: " + response.getStatusCode());
                })
                .body(String.class);

        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            throw new BusinessException("API返回数据为空");
        }

        try {
            PconlineIpResponse apiResponse = objectMapper.readValue(jsonResponse, PconlineIpResponse.class);

            if (apiResponse.getErr() != null && !apiResponse.getErr().trim().isEmpty()) {
                throw new BusinessException("API调用失败: " + apiResponse.getErr());
            }

            return convertToResponse(apiResponse);
        } catch (Exception e) {
            log.error("解析API响应失败: {}", jsonResponse, e);
            throw new BusinessException("API响应解析失败");
        }
    }

    private CachedIpResponse convertToResponse(PconlineIpResponse data) {
        if (data == null) {
            throw new BusinessException("API返回数据为空");
        }
        return CachedIpResponse.builder()
                .ip(data.getIp())
                .country("中国")
                .region(data.getPro())
                .city(data.getCity())
                .isp(parseIsp(data.getAddr()))
                .regionId(data.getProCode())
                .cityId(data.getCityCode())
                .countryId("CN")
                .build();
    }

    private String parseIsp(String addr) {
        if (addr == null || addr.trim().isEmpty()) {
            return "";
        }
        String[] parts = addr.split(" ");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
    }
}
