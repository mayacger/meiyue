package com.meiyuemall.common.tenant;

import com.meiyuemall.common.security.MeiyuePrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 租户上下文生命周期过滤器。
 * <p>
 * 职责：请求结束后 {@link TenantContext#clear()}，防止线程池串租。
 * 上下文写入由 JWT 鉴权过滤器在认证成功后调用 {@link #applyFromPrincipal} 完成。
 * <b>不信任</b>客户端传入的 tenant 头。
 * </p>
 * <p>
 * 仅通过 SecurityFilterChain 注册（见 boot SecurityConfig），避免与 Servlet 容器双重注册。
 * </p>
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * JWT 认证成功后写入请求级租户上下文。
     *
     * @param principal 当前用户主体；null 则写入匿名
     */
    public static void applyFromPrincipal(MeiyuePrincipal principal) {
        if (principal == null) {
            TenantContext.set(new TenantContext.Holder(null, ActorType.ANONYMOUS, null, null));
            return;
        }
        TenantContext.set(new TenantContext.Holder(
                principal.getTenantId(),
                principal.getActorType(),
                principal.getUserId(),
                principal.getStoreId()
        ));
    }
}
