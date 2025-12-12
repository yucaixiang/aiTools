package com.toolrecommend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 * 从Gateway迁移过来
 *
 * @author Tool Recommend Team
 */
@Slf4j
@Component
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${auth.whitelist}")
    private String whitelistStr;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        log.info("========== JWT认证过滤器 ==========");
        log.info("请求: {} {}", method, path);

        // OPTIONS请求直接放行(CORS预检)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.info("OPTIONS请求,直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        // 检查是否在白名单中
        if (isWhitelisted(path)) {
            log.info("白名单路径，跳过认证: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // 获取Token
        String authHeader = request.getHeader("Authorization");
        log.info("Authorization Header: {}", authHeader);

        String token = extractToken(request);
        if (token == null) {
            log.warn("缺少Token: {}", path);
            sendUnauthorized(response, "未授权，请先登录");
            return;
        }

        log.info("提取的Token: {}", token.substring(0, Math.min(20, token.length())) + "...");

        // 验证Token
        try {
            log.info("开始验证Token...");
            if (!JwtUtil.validateToken(token)) {
                log.warn("Token验证失败: Token无效或已过期");
                sendUnauthorized(response, "Token无效或已过期");
                return;
            }

            // 从Token中提取用户信息
            Long userId = JwtUtil.getUserIdFromToken(token);
            String username = JwtUtil.getUsernameFromToken(token);
            String role = JwtUtil.getRoleFromToken(token);

            log.info("用户认证成功: userId={}, username={}, role={}", userId, username, role);

            // 将用户信息添加到请求属性，供后续使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("userRole", role);

            // 同时将用户信息添加到请求头，兼容使用@RequestHeader的Controller
            // 注意: 不能直接修改HttpServletRequest的header,需要使用wrapper
            HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    log.info("🔍 Wrapper.getHeader被调用: name={}", name);
                    if ("X-User-Id".equalsIgnoreCase(name)) {
                        log.info("✅ 返回X-User-Id: {}", userId);
                        return String.valueOf(userId);
                    } else if ("Username".equalsIgnoreCase(name)) {
                        log.info("✅ 返回Username: {}", username);
                        return username;
                    } else if ("User-Role".equalsIgnoreCase(name)) {
                        log.info("✅ 返回User-Role: {}", role);
                        return role;
                    }
                    String originalValue = super.getHeader(name);
                    log.info("📋 返回原始header: {}={}", name, originalValue);
                    return originalValue;
                }

                @Override
                public java.util.Enumeration<String> getHeaders(String name) {
                    log.info("🔍 Wrapper.getHeaders被调用: name={}", name);
                    if ("X-User-Id".equalsIgnoreCase(name)) {
                        return java.util.Collections.enumeration(java.util.Collections.singletonList(String.valueOf(userId)));
                    } else if ("Username".equalsIgnoreCase(name)) {
                        return java.util.Collections.enumeration(java.util.Collections.singletonList(username));
                    } else if ("User-Role".equalsIgnoreCase(name)) {
                        return java.util.Collections.enumeration(java.util.Collections.singletonList(role));
                    }
                    return super.getHeaders(name);
                }

                @Override
                public java.util.Enumeration<String> getHeaderNames() {
                    log.info("🔍 Wrapper.getHeaderNames被调用");
                    java.util.Set<String> headerNames = new java.util.HashSet<>();
                    java.util.Enumeration<String> originalNames = super.getHeaderNames();
                    while (originalNames.hasMoreElements()) {
                        headerNames.add(originalNames.nextElement());
                    }
                    headerNames.add("X-User-Id");
                    headerNames.add("Username");
                    headerNames.add("User-Role");
                    return java.util.Collections.enumeration(headerNames);
                }
            };

            log.info("🔄 传递requestWrapper到filter chain");
            filterChain.doFilter(requestWrapper, response);

        } catch (Exception e) {
            log.error("Token验证异常: {}, 异常类型: {}", e.getMessage(), e.getClass().getSimpleName(), e);
            sendUnauthorized(response, "Token验证失败: " + e.getMessage());
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelisted(String path) {
        List<String> whitelist = Arrays.asList(whitelistStr.split(","));
        return whitelist.stream()
                .map(String::trim)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 返回未授权响应
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Result<Void> result = Result.error(401, message);
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().write(json);
    }
}
