package dev.qiuyun.qiuyuntoolbackend.util.ip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.model.dto.TaobaoIpData;
import dev.qiuyun.qiuyuntoolbackend.model.dto.TaobaoIpResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 淘宝IP API客户端
 *
 * 功能特性：
 * - 多级缓存策略：Caffeine本地缓存(24小时) → Redis缓存(7天) → API调用
 * - 使用Spring Retry实现自动重试，最多重试3次，指数退避策略
 * - 使用ObjectMapper进行JSON序列化/反序列化
 * - 支持超时配置，默认30秒
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
public class TaobaoIpApiClient {

    private static final String API_URL = "https://ip.taobao.com/outGetIpInfo";
    private static final String ACCESS_KEY = "alibaba-inc";
    private static final String CACHE_PREFIX = "ip:query:";
    private static final long LOCAL_CACHE_TTL_HOURS = 24;
    private static final long REDIS_CACHE_TTL_DAYS = 7;
    private static final int TIMEOUT = 30000;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    private final Cache<String, CachedIpResponse> localCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(LOCAL_CACHE_TTL_HOURS, TimeUnit.HOURS)
            .build();

    /**
     * 构造函数
     * 初始化RestTemplate，设置连接和读取超时
     */
    public TaobaoIpApiClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT);
        factory.setReadTimeout(TIMEOUT);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 查询IP地址信息
     *
     * 流程：
     * 1. 先查本地缓存（Caffeine），命中则直接返回
     * 2. 本地缓存未命中则查Redis缓存
     * 3. Redis缓存未命中则调用淘宝IP API
     * 4. API返回结果写入两级缓存
     *
     * @param ip 要查询的IP地址
     * @return IP查询结果
     * @throws BusinessException 查询失败时抛出
     */
    public CachedIpResponse queryIp(String ip) {
        CachedIpResponse localResult = localCache.getIfPresent(ip);
        if (localResult != null) {
            localResult.setSource("local");
            log.debug("本地缓存命中: {}", ip);
            return localResult;
        }

        String redisKey = CACHE_PREFIX + ip;
        CachedIpResponse redisResult = getFromRedis(redisKey);
        if (redisResult != null) {
            redisResult.setSource("redis");
            log.debug("Redis缓存命中: {}", ip);
            localCache.put(ip, redisResult);
            return redisResult;
        }

        try {
            CachedIpResponse apiResult = queryFromApiWithRetry(ip);
            apiResult.setSource("api");
            apiResult.setQueryTime(System.currentTimeMillis());

            localCache.put(ip, apiResult);
            setToRedis(redisKey, apiResult);

            log.info("API查询成功: {}", ip);
            return apiResult;
        } catch (Exception e) {
            log.error("IP查询失败: {}", ip, e);
            throw new BusinessException("IP查询失败，请稍后重试");
        }
    }

    /**
     * 仅从本地缓存查询
     *
     * @param ip 要查询的IP地址
     * @return 本地缓存结果，未命中返回null
     */
    public CachedIpResponse queryFromLocalCache(String ip) {
        CachedIpResponse result = localCache.getIfPresent(ip);
        if (result != null) {
            result.setSource("local");
        }
        return result;
    }

    /**
     * 从Redis读取缓存
     *
     * @param key Redis键
     * @return 反序列化后的缓存对象，读取失败返回null
     */
    private CachedIpResponse getFromRedis(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, CachedIpResponse.class);
            }
        } catch (Exception e) {
            log.warn("从Redis读取失败: {}", key, e);
        }
        return null;
    }

    /**
     * 写入Redis缓存
     *
     * @param key Redis键
     * @param response 要缓存的响应对象
     */
    private void setToRedis(String key, CachedIpResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(key, json, REDIS_CACHE_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("写入Redis失败: {}", key, e);
        }
    }

    /**
     * 带重试机制的API调用
     *
     * 重试策略：
     * - 最多重试3次
     * - 初始延迟1000毫秒
     * - 每次重试延迟翻倍（指数退避）
     *
     * @param ip 要查询的IP地址
     * @return API返回的IP信息
     * @throws Exception 调用失败时抛出
     */
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    private CachedIpResponse queryFromApiWithRetry(String ip) {
        String url = API_URL + "?ip=" + ip + "&accessKey=" + ACCESS_KEY;
        log.debug("调用淘宝API: {}", url);

        ResponseEntity<TaobaoIpResponse> response = restTemplate.getForEntity(url, TaobaoIpResponse.class);
        TaobaoIpResponse apiResponse = response.getBody();

        if (apiResponse == null || apiResponse.getCode() != 0) {
            throw new BusinessException("API调用失败: " + (apiResponse != null ? apiResponse.getMsg() : "未知错误"));
        }

        return convertToResponse(apiResponse.getData());
    }

    /**
     * 将淘宝API响应转换为内部响应格式
     *
     * @param data 淘宝API返回的原始数据
     * @return 转换后的响应对象
     */
    private CachedIpResponse convertToResponse(TaobaoIpData data) {
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

    /**
     * 缓存的IP查询响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CachedIpResponse {
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
