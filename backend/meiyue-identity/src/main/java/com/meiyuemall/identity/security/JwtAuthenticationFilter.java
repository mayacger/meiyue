package com.meiyuemall.identity.security;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.tenant.TenantContextFilter;
import com.meiyuemall.identity.domain.UserAccount;
import com.meiyuemall.identity.repo.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 鉴权过滤器：解析 Bearer Token → SecurityContext + TenantContext。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;
    private final PrincipalFactory principalFactory;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserAccountRepository userAccountRepository,
            PrincipalFactory principalFactory
    ) {
        this.jwtService = jwtService;
        this.userAccountRepository = userAccountRepository;
        this.principalFactory = principalFactory;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            String token = header.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
            try {
                JwtService.ParsedToken parsed = jwtService.parse(token);
                UserAccount user = userAccountRepository.findById(parsed.userId()).orElse(null);
                if (user != null) {
                    var principal = principalFactory.fromUser(user);
                    var authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    TenantContextFilter.applyFromPrincipal(principal);
                }
            } catch (Exception ex) {
                log.debug("JWT 无效，按匿名继续: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
