package com.meiyuemall.common.security;

/**
 * 安全相关常量（脚手架）。
 * <p>
 * JWT / Session 细节在 I1 由 meiyue-identity 落地；此处仅集中公开路径与角色名约定。
 * </p>
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    /** API 版本前缀，与规划 §1.3 一致 */
    public static final String API_PREFIX = "/api/v1";

    /** 健康检查与 Actuator（可按环境收紧） */
    public static final String[] PUBLIC_PATHS = {
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            API_PREFIX + "/ping"
    };

    /** Spring Security 角色名前缀约定 */
    public static final String ROLE_BUYER = "ROLE_BUYER";
    public static final String ROLE_SELLER = "ROLE_SELLER";
    public static final String ROLE_PLATFORM = "ROLE_PLATFORM";
}
