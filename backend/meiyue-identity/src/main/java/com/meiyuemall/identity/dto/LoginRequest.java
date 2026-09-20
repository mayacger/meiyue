package com.meiyuemall.identity.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求。
 *
 * @param username 登录名
 * @param password 明文密码
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
