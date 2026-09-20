package com.meiyuemall.common.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 多租户 {@code tenant_id} 过滤器骨架。
 * <p>
 * 本轮行为（脚手架）：
 * <ol>
 *   <li>尝试从请求头 {@value #HEADER_TENANT_ID} 读取租户（仅用于本地联调演示，生产不可信）</li>
 *   <li>写入 {@link TenantContext}；默认 {@link ActorType#ANONYMOUS}</li>
 *   <li>请求结束清理 ThreadLocal，防止线程池串租</li>
 * </ol>
 * I1 起应由 JWT/Session 鉴权结果注入，并忽略客户端伪造的 tenant 头。
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);

    /**
     * 联调占位请求头；正式环境不得作为租户权威来源。
     */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    /**
     * 联调占位：参与者类型头，取值 BUYER / SELLER / PLATFORM / ANONYMOUS。
     */
    public static final String HEADER_ACTOR_TYPE = "X-Actor-Type";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            Long tenantId = parseLongHeader(request.getHeader(HEADER_TENANT_ID));
            ActorType actorType = parseActorType(request.getHeader(HEADER_ACTOR_TYPE));

            // 脚手架阶段：仅写入上下文，不做鉴权与串租校验（I1/I7 补齐）
            TenantContext.set(new TenantContext.Holder(tenantId, actorType, null, tenantId));

            if (log.isDebugEnabled()) {
                log.debug(
                        "TenantContext 已注入 path={} tenantId={} actorType={}",
                        request.getRequestURI(),
                        tenantId,
                        actorType
                );
            }

            filterChain.doFilter(request, response);
        } finally {
            // 关键：必须清理，避免 Tomcat 线程复用导致串租
            TenantContext.clear();
        }
    }

    private static Long parseLongHeader(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            log.warn("非法租户头，已忽略: {}", raw);
            return null;
        }
    }

    private static ActorType parseActorType(String raw) {
        if (raw == null || raw.isBlank()) {
            return ActorType.ANONYMOUS;
        }
        try {
            return ActorType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ActorType.ANONYMOUS;
        }
    }
}
