package com.toolrecommend.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT认证过滤器
 *
 * @author Tool Recommend Team
 */
@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Value("#{'${auth.whitelist}'.split(',')}")
    private List<String> whitelist;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.debug("请求路径: {}", path);

        // 检查是否在白名单中
        if (isWhitelisted(path)) {
            log.debug("白名单路径，跳过认证: {}", path);
            return chain.filter(exchange);
        }

        // 获取Token
        String token = extractToken(request);
        if (token == null) {
            log.warn("缺少Token: {}", path);
            return unauthorized(exchange.getResponse(), "未授权，请先登录");
        }

        // 验证Token
        try {
            if (!JwtUtil.validateToken(token)) {
                log.warn("Token无效或已过期: {}", path);
                return unauthorized(exchange.getResponse(), "Token无效或已过期");
            }

            // 从Token中提取用户信息
            Long userId = JwtUtil.getUserIdFromToken(token);
            String username = JwtUtil.getUsernameFromToken(token);
            String role = JwtUtil.getRoleFromToken(token);

            log.debug("用户认证成功: userId={}, username={}, role={}", userId, username, role);

            // 将用户信息添加到请求头，传递给下游服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("User-Id", String.valueOf(userId))
                    .header("Username", username)
                    .header("User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return unauthorized(exchange.getResponse(), "Token验证失败");
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelisted(String path) {
        return whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        Result<Void> result = Result.error(401, message);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            log.error("序列化错误响应失败", e);
            bytes = "{\"code\":401,\"message\":\"未授权\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 优先级高，先执行认证
    }
}
