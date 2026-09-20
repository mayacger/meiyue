package com.meiyuemall.common.security;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * I7：关键接口内存滑动窗口限流（无 Redis 时的基础防护）。
 * <ul>
 *   <li>登录：按 IP</li>
 *   <li>支付回调：按 IP</li>
 *   <li>其它 API：按 IP（宽松阈值）</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int loginPerMinute;
    private final int notifyPerMinute;
    private final int apiPerMinute;

    /** key → 窗口计数 */
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${meiyue.rate-limit.enabled:true}") boolean enabled,
            @Value("${meiyue.rate-limit.login-per-minute:30}") int loginPerMinute,
            @Value("${meiyue.rate-limit.notify-per-minute:60}") int notifyPerMinute,
            @Value("${meiyue.rate-limit.api-per-minute:300}") int apiPerMinute
    ) {
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
        int limit = resolveLimit(path);
        if (limit > 0) {
            String key = limit + "|" + pathBucket(path) + "|" + ip;
            if (!tryAcquire(key, limit)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"success\":false,\"code\":\"RATE_LIMITED\",\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private int resolveLimit(String path) {
        if (path.startsWith(SecurityConstants.API_PREFIX + "/auth/login")
                || path.startsWith(SecurityConstants.API_PREFIX + "/auth/register")) {
            return loginPerMinute;
        }
        if (path.startsWith(SecurityConstants.API_PREFIX + "/payments/notify")) {
            return notifyPerMinute;
        }
        if (path.startsWith(SecurityConstants.API_PREFIX)) {
            return apiPerMinute;
        }
        return 0;
    }

    private static String pathBucket(String path) {
        if (path.contains("/auth/login")) return "login";
        if (path.contains("/auth/register")) return "register";
        if (path.contains("/payments/notify")) return "notify";
        return "api";
    }

    private boolean tryAcquire(String key, int limit) {
        long now = System.currentTimeMillis();
        Window w = windows.compute(key, (k, old) -> {
            if (old == null || now - old.windowStartMs >= 60_000L) {
                return new Window(now, new AtomicInteger(1));
            }
            old.count.incrementAndGet();
            return old;
        });
        // 偶然竞态下可能略超，MVP 可接受
        return w.count.get() <= limit;
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }

    private record Window(long windowStartMs, AtomicInteger count) {}
}
