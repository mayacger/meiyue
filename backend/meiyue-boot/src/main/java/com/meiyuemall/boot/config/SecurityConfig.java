package com.meiyuemall.boot.config;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.tenant.TenantContextFilter;
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
 * Spring Security 基础配置（脚手架）。
 * <p>
 * 本轮：启用过滤器链与方法级安全开关；公开 ping/health；其余暂放行。
 * I1 接入 JWT/Session 后改为 {@code authenticated()}，并按 RBAC 收紧。
 * </p>
 * <p>
 * 过滤器顺序：{@link TenantContextFilter} 在 UsernamePasswordAuthenticationFilter 之前，
 * 保证后续业务能读到 TenantContext（正式鉴权后仍保持此顺序）。
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TenantContextFilter tenantContextFilter;

    public SecurityConfig(TenantContextFilter tenantContextFilter) {
        this.tenantContextFilter = tenantContextFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API 服务默认 CSRF 关闭（无浏览器表单会话）；若改 Session Cookie 需重新评估
                .csrf(AbstractHttpConfigurer::disable)
                // 脚手架不启 httpBasic，避免生成随机默认用户密码告警；I1 换 JWT
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstants.PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 脚手架：其余路径先放行，避免无用户体系时无法联调；I1 改为 authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(tenantContextFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
