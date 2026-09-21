package com.meiyuemall.identity.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求（I35 可选验证码）。
 *
 * @param username    登录名
 * @param password    明文密码
 * @param captchaId   验证码 ID（开启时必填）
 * @param captchaCode 验证码答案
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password,
        String captchaId,
        String captchaCode
) {
}
