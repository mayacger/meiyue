package com.meiyuemall.identity.dto;

import java.util.Set;

/**
 * 登录/注册成功响应。
 *
 * @param accessToken JWT 访问令牌
 * @param tokenType   固定 Bearer
 * @param expiresIn   有效期秒数
 * @param user        当前用户摘要
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserProfileResponse user
) {
}
