package com.meiyuemall.common.security;

/**
 * 安全相关常量（I1）。
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    /** API 版本前缀 */
    public static final String API_PREFIX = "/api/v1";

    /** Authorization: Bearer &lt;token&gt; */
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    /** 公开路径：健康检查、探活、注册登录 */
    public static final String[] PUBLIC_PATHS = {
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            API_PREFIX + "/ping",
            API_PREFIX + "/auth/register",
            API_PREFIX + "/auth/login"
    };

    /** Spring Security 角色名（带 ROLE_ 前缀） */
    public static final String ROLE_BUYER = "ROLE_BUYER";
    public static final String ROLE_SELLER_OWNER = "ROLE_SELLER_OWNER";
    public static final String ROLE_SELLER_STAFF = "ROLE_SELLER_STAFF";
    public static final String ROLE_PLATFORM_ADMIN = "ROLE_PLATFORM_ADMIN";
}
