package com.meiyuemall.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 店主邀请店员请求（I26）。
 *
 * @param username 已注册用户的登录名（通常为买家账号）
 */
public record InviteStaffRequest(
        @NotBlank @Size(max = 64) String username
) {}
