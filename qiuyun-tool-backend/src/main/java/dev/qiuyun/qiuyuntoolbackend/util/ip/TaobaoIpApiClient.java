package dev.qiuyun.qiuyuntoolbackend.util.ip;

import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.model.dto.TaobaoIpData;
import dev.qiuyun.qiuyuntoolbackend.model.dto.TaobaoIpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 淘宝IP API客户端
 *
 * 接口地址：https://ip.taobao.com/outGetIpInfo?ip=218.201.25.123&amp;accessKey=alibaba-inc
 * 成功响应示例：
 * {"data":{"area":"","country":"中国","isp_id":"100025","queryIp":"218.201.25.123","city":"重庆","ip":"218.201.25.123","isp":"移动","county":"","region_id":"500000","area_id":"","county_id":null,"region":"重庆","country_id":"CN","city_id":"500100"},"msg":"query success","code":0}
 * 失败响应示例：
 * {"msg":"the request over max qps for user ,the accessKey=alibaba-inc","code":4}
 *
 * 功能特性：
 * - 多级缓存策略：Caffeine本地缓存(24小时) → Redis缓存(7天) → API调用
 * - 使用Spring Retry实现自动重试，最多重试3次，指数退避策略
 * - 使用ObjectMapper进行JSON序列化/反序列化
 * - 使用RestClient进行HTTP请求
 *
 * 缓存策略：
 * 1. 优先查询本地缓存（Caffeine），命中立即返回
 * 2. 本地缓存未命中则查询Redis缓存
 * 3. Redis缓存未命中则调用淘宝IP API
 * 4. API返回结果同时写入两级缓存
 *
 * @author qiuyun
 */
@Slf4j
@Component
public class TaobaoIpApiClient extends AbstractIpApiClient {

    private static final String ACCESS_KEY = "alibaba-inc";
    private static final String CACHE_PREFIX = "ip:query:";

    public TaobaoIpApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://ip.taobao.com")
                .build();
        initCache();
    }

    @Override
    protected String getCachePrefix() {
        return CACHE_PREFIX;
    }

    @Override
    protected CachedIpResponse doQueryFromApi(String ip) {
        log.debug("调用淘宝API: ip={}", ip);

        TaobaoIpResponse apiResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/outGetIpInfo")
                        .queryParam("ip", ip)
                        .queryParam("accessKey", ACCESS_KEY)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new BusinessException("API调用失败，状态码: " + response.getStatusCode());
                })
                .body(TaobaoIpResponse.class);

        if (apiResponse == null || apiResponse.getCode() != 0) {
            throw new BusinessException("API调用失败: " + (apiResponse != null ? apiResponse.getMsg() : "未知错误"));
        }

        return convertToResponse(apiResponse.getData());
    }

    private CachedIpResponse convertToResponse(TaobaoIpData data) {
        if (data == null) {
            throw new BusinessException("API返回数据为空");
        }
        return CachedIpResponse.builder()
                .ip(data.getIp())
                .country(data.getCountry())
                .region(data.getRegion())
                .city(data.getCity())
                .isp(data.getIsp())
                .regionId(data.getRegion_id())
                .cityId(data.getCity_id())
                .countryId(data.getCountry_id())
                .build();
    }
}
