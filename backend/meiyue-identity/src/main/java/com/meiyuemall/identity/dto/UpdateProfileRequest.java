package com.meiyuemall.identity.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新个人资料请求（I24）。
 * <p>空字段表示不修改；username 不可改。</p>
 *
 * @param displayName 展示名
 * @param phone       手机号（可空串清空）
 */
public record UpdateProfileRequest(
        @Size(max = 128) String displayName,
        @Size(max = 32) String phone
) {}
