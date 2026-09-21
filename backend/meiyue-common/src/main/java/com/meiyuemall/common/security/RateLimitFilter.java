package com.meiyuemall.common.security;

import com.meiyuemall.common.redis.RateLimitPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * I7/I9：关键接口限流（优先 Redis，失败降级内存）。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int loginPerMinute;
    private final int notifyPerMinute;
    private final int apiPerMinute;
    private final RateLimitPort rateLimitPort;

    public RateLimitFilter(
            RateLimitPort rateLimitPort,
            @Value("${meiyue.rate-limit.enabled:true}") boolean enabled,
            @Value("${meiyue.rate-limit.login-per-minute:30}") int loginPerMinute,
            @Value("${meiyue.rate-limit.notify-per-minute:60}") int notifyPerMinute,
            @Value("${meiyue.rate-limit.api-per-minute:300}") int apiPerMinute
    ) {
        this.rateLimitPort = rateLimitPort;
        this.enabled = enabled;
        this.loginPerMinute = loginPerMinute;
        this.notifyPerMinute = notifyPerMinute;
        this.apiPerMinute = apiPerMinute;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        String ip = clientIp(request);
        String bucket = pathBucket(path);
        int limit = resolveLimit(bucket);
        if (limit > 0 && !rateLimitPort.tryAcquire(bucket, ip, limit, 60)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"code\":\"RATE_LIMITED\",\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private int resolveLimit(String bucket) {
        return switch (bucket) {
            case "login", "register" -> loginPerMinute;
            case "notify" -> notifyPerMinute;
            case "api" -> apiPerMinute;
            default -> 0;
        };
    }

    private static String pathBucket(String path) {
        if (path.startsWith(SecurityConstants.API_PREFIX + "/auth/login")) return "login";
        if (path.startsWith(SecurityConstants.API_PREFIX + "/auth/register")) return "register";
        if (path.startsWith(SecurityConstants.API_PREFIX + "/payments/notify")) return "notify";
        if (path.startsWith(SecurityConstants.API_PREFIX)) return "api";
        return "none";
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }
}
