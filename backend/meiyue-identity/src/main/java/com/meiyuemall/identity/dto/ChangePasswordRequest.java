package com.meiyuemall.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求（I24）。
 *
 * @param oldPassword 当前密码（明文，仅传输）
 * @param newPassword 新密码 6~64
 */
public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 6, max = 64) String newPassword
) {}
