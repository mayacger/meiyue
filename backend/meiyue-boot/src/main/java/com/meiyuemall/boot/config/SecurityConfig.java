package com.meiyuemall.boot.config;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.tenant.TenantContextFilter;
import com.meiyuemall.identity.security.JwtAuthenticationFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security（I1）：JWT + RBAC。
 * <p>
 * 过滤器顺序：
 * TenantContextFilter（清理）→ JwtAuthenticationFilter（鉴权+注入租户）→ …
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TenantContextFilter tenantContextFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            TenantContextFilter tenantContextFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.tenantContextFilter = tenantContextFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstants.PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // I7：Actuator 除健康/信息外需认证（防指标与内部端点裸奔）
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").authenticated()
                        // I2 买家公开浏览
                        .requestMatchers(HttpMethod.GET,
                                SecurityConstants.API_PREFIX + "/categories",
                                SecurityConstants.API_PREFIX + "/products",
                                SecurityConstants.API_PREFIX + "/products/**",
                                SecurityConstants.API_PREFIX + "/stores/*/products",
                                SecurityConstants.API_PREFIX + "/stores/*/page",
                                SecurityConstants.API_PREFIX + "/decoration/templates"
                        ).permitAll()
                        // I4 支付通道回调（验签在业务内完成）
                        .requestMatchers(HttpMethod.POST,
                                SecurityConstants.API_PREFIX + "/payments/notify/**"
                        ).permitAll()
                        .requestMatchers(SecurityConstants.API_PREFIX + "/admin/**")
                        .hasRole("PLATFORM_ADMIN")
                        .requestMatchers(SecurityConstants.API_PREFIX + "/seller/store")
                        .hasAnyRole("SELLER_OWNER", "SELLER_STAFF")
                        .requestMatchers(
                                SecurityConstants.API_PREFIX + "/seller/products",
                                SecurityConstants.API_PREFIX + "/seller/products/**"
                        ).hasAnyRole("SELLER_OWNER", "SELLER_STAFF")
                        .requestMatchers(
                                SecurityConstants.API_PREFIX + "/seller/decoration",
                                SecurityConstants.API_PREFIX + "/seller/decoration/**"
                        ).hasAnyRole("SELLER_OWNER", "SELLER_STAFF")
                        .requestMatchers(
                                SecurityConstants.API_PREFIX + "/seller/shipments",
                                SecurityConstants.API_PREFIX + "/seller/shipments/**",
                                SecurityConstants.API_PREFIX + "/seller/aftersales",
                                SecurityConstants.API_PREFIX + "/seller/aftersales/**",
                                SecurityConstants.API_PREFIX + "/seller/ai",
                                SecurityConstants.API_PREFIX + "/seller/ai/**"
                        ).hasAnyRole("SELLER_OWNER", "SELLER_STAFF")
                        .requestMatchers(SecurityConstants.API_PREFIX + "/seller/onboarding/**")
                        .authenticated()
                        .requestMatchers(SecurityConstants.API_PREFIX + "/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"success\":false,\"code\":\"UNAUTHORIZED\",\"message\":\"未登录或凭证无效\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"success\":false,\"code\":\"FORBIDDEN\",\"message\":\"无权限访问该资源\",\"data\":null}");
                        })
                )
                .addFilterBefore(tenantContextFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, TenantContextFilter.class);

        return http.build();
    }

    /**
     * 禁用 Servlet 容器对 OncePerRequestFilter Bean 的自动注册，仅走 Security 链，避免执行两次。
     */
    @Bean
    public FilterRegistrationBean<Filter> disableTenantFilterRegistration(TenantContextFilter filter) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<Filter> disableJwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
