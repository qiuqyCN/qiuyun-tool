package dev.qiuyun.qiuyuntoolbackend.util.ip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.RateLimiter;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractIpApiClient {

    protected static final String NULL_CACHE_MARKER = "NULL_CACHE_MARKER";
    protected static final long LOCAL_CACHE_TTL_HOURS = 24;
    protected static final long NULL_CACHE_TTL_MINUTES = 5;
    protected static final long REDIS_CACHE_TTL_DAYS = 7;

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected RetryTemplate retryTemplate;

    protected RestClient restClient;
    protected RateLimiter rateLimiter;

    protected Cache<String, CachedIpResponse> localCache;

    protected void initCache() {
        this.localCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(LOCAL_CACHE_TTL_HOURS, TimeUnit.HOURS)
                .build();
    }

    protected abstract String getCachePrefix();

    protected abstract CachedIpResponse doQueryFromApi(String ip);

    protected boolean useRateLimiter() {
        return false;
    }

    public CachedIpResponse queryIp(String ip) {
        CachedIpResponse localResult = localCache.getIfPresent(ip);
        if (localResult != null) {
            if (NULL_CACHE_MARKER.equals(localResult.getSource())) {
                log.debug("本地缓存命中(空结果保护): {}", ip);
                throw new BusinessException("IP查询失败：无效的IP地址");
            }
            localResult.setSource("local");
            log.debug("本地缓存命中: {}", ip);
            return localResult;
        }

        String redisKey = getCachePrefix() + ip;
        CachedIpResponse redisResult = getFromRedis(redisKey);
        if (redisResult != null) {
            if (NULL_CACHE_MARKER.equals(redisResult.getSource())) {
                log.debug("Redis缓存命中(空结果保护): {}", ip);
                localCache.put(ip, redisResult);
                throw new BusinessException("IP查询失败：无效的IP地址");
            }
            redisResult.setSource("redis");
            log.debug("Redis缓存命中: {}", ip);
            localCache.put(ip, redisResult);
            return redisResult;
        }

        try {
            CachedIpResponse apiResult = retryTemplate.execute(context -> {
                log.info("API查询尝试: ip={}, attempt={}", ip, context.getRetryCount() + 1);
                return doQueryFromApi(ip);
            });
            apiResult.setSource("api");
            apiResult.setQueryTime(System.currentTimeMillis());

            localCache.put(ip, apiResult);
            setToRedis(redisKey, apiResult);

            log.info("API查询成功: {}", ip);
            return apiResult;
        } catch (Exception e) {
            log.warn("IP查询失败，缓存空结果: ip={}, error={}", ip, e.getMessage());
            CachedIpResponse nullResult = CachedIpResponse.builder()
                    .source(NULL_CACHE_MARKER)
                    .build();
            localCache.put(ip, nullResult);
            setNullToRedis(redisKey);
            throw new BusinessException("IP查询失败，请稍后重试");
        }
    }

    public CachedIpResponse queryFromLocalCache(String ip) {
        CachedIpResponse result = localCache.getIfPresent(ip);
        if (result != null) {
            result.setSource("local");
        }
        return result;
    }

    protected CachedIpResponse getFromRedis(String key) {
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

    protected void setToRedis(String key, CachedIpResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(key, json, REDIS_CACHE_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("写入Redis失败: {}", key, e);
        }
    }

    protected void setNullToRedis(String key) {
        try {
            CachedIpResponse nullResult = CachedIpResponse.builder()
                    .source(NULL_CACHE_MARKER)
                    .build();
            String json = objectMapper.writeValueAsString(nullResult);
            redisTemplate.opsForValue().set(key, json, NULL_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写入Redis空结果失败: {}", key, e);
        }
    }

    protected void acquireRateLimiter() {
        if (useRateLimiter() && rateLimiter != null) {
            rateLimiter.acquire();
        }
    }
}
