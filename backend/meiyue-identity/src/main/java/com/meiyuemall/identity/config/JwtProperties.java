package com.meiyuemall.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置项（绑定 {@code meiyue.security.jwt.*}）。
 *
 * @param secret          HMAC 密钥（本地开发默认值；生产必须覆盖）
 * @param issuer          签发方标识
 * @param expireSeconds   访问令牌有效期（秒）
 */
@ConfigurationProperties(prefix = "meiyue.security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        long expireSeconds
) {
}
