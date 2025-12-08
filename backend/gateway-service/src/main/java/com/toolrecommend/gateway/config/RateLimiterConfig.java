package com.toolrecommend.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * 限流配置
 *
 * @author Tool Recommend Team
 */
@Configuration
public class RateLimiterConfig {

    /**
     * 基于IP的限流Key解析器（默认）
     * 使用@Primary标记为主要的KeyResolver，解决多个Bean冲突问题
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = "unknown";
            var remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress != null && remoteAddress.getAddress() != null) {
                ip = remoteAddress.getAddress().getHostAddress();
            }
            return Mono.just(ip);
        };
    }

    /**
     * 基于用户的限流Key解析器
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

    /**
     * 基于接口路径的限流Key解析器
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            return Mono.just(path);
        };
    }
}
