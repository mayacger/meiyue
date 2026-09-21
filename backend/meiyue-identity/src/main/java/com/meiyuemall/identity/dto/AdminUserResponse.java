package com.meiyuemall.identity.dto;

import com.meiyuemall.identity.domain.RoleCode;
import com.meiyuemall.identity.domain.UserStatus;

import java.time.Instant;
import java.util.Set;

/**
 * 平台用户目录只读项（I18）。
 *
 * @param id          主键
 * @param username    登录名
 * @param displayName 展示名
 * @param phone       手机，可空
 * @param status      ENABLED / DISABLED
 * @param roles       角色集合
 * @param createdAt   创建时间
 */
public record AdminUserResponse(
        Long id,
        String username,
        String displayName,
        String phone,
        UserStatus status,
        Set<RoleCode> roles,
        Instant createdAt
) {}
