package dev.qiuyun.qiuyuntoolbackend.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * CORS 全局配置
 * 使用过滤器方式配置，优先级最高
 */
@Configuration
public class CorsConfig {

    /**
     * 配置 CORS 过滤器
     * 设置为最高优先级，确保在其他过滤器之前执行
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        
        // 允许所有请求方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        
        // 允许所有请求头
        config.setAllowedHeaders(Collections.singletonList("*"));
        
        // 暴露所有响应头
        config.setExposedHeaders(Collections.singletonList("*"));
        
        // 不允许携带凭证（当使用 * 通配符时必须为 false）
        config.setAllowCredentials(false);
        
        // 预检请求缓存时间（1小时）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        // 设置为最高优先级
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        
        return bean;
    }
}
