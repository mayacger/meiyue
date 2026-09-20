package com.meiyuemall.identity.security;

import com.meiyuemall.identity.config.JwtProperties;
import com.meiyuemall.identity.domain.RoleCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT 签发与解析（HMAC-SHA）。
 * <p>
 * Claims：
 * <ul>
 *   <li>{@code sub} — 用户 ID 字符串</li>
 *   <li>{@code username} — 登录名</li>
 *   <li>{@code roles} — 逗号分隔角色码</li>
 * </ul>
 * 租户 ID 不写入 token，每次请求按 seller_members 实时解析，避免审核通过后需强刷 token。
 * </p>
 */
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        // 密钥长度需满足 HMAC-SHA 要求；配置项请使用足够长的随机串
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueToken(Long userId, String username, Collection<RoleCode> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.expireSeconds());
        String rolesClaim = roles.stream().map(Enum::name).collect(Collectors.joining(","));
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roles", rolesClaim)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public ParsedToken parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        String rolesRaw = claims.get("roles", String.class);
        Set<RoleCode> roles = Arrays.stream((rolesRaw == null ? "" : rolesRaw).split(","))
                .filter(s -> !s.isBlank())
                .map(RoleCode::valueOf)
                .collect(Collectors.toSet());
        return new ParsedToken(userId, username, roles);
    }

    /**
     * @param userId   用户 ID
     * @param username 登录名
     * @param roles    角色集合
     */
    public record ParsedToken(Long userId, String username, Set<RoleCode> roles) {
    }
}
