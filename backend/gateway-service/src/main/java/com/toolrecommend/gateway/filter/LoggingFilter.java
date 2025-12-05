package com.toolrecommend.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 日志过滤器
 *
 * @author Tool Recommend Team
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        String method = request.getMethod().name();
        String path = request.getPath().value();
        String ip = request.getRemoteAddress().getAddress().getHostAddress();

        log.info("==> {} {} from {}", method, path, ip);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            int statusCode = exchange.getResponse().getStatusCode().value();

            log.info("<== {} {} {} - {}ms", method, path, statusCode, duration);
        }));
    }

    @Override
    public int getOrder() {
        return -200; // 优先级最高，最先执行
    }
}
