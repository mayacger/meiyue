package com.meiyuemall.identity.dto;

import java.util.Set;

/**
 * 当前用户资料。
 *
 * @param id          用户 ID
 * @param username    登录名
 * @param displayName 展示名
 * @param phone       手机
 * @param roles       角色码集合
 * @param tenantId    商家租户（无则为 null）
 * @param storeId     店铺 ID（无则为 null）
 * @param actorType   参与者类型名
 */
public record UserProfileResponse(
        Long id,
        String username,
        String displayName,
        String phone,
        Set<String> roles,
        Long tenantId,
        Long storeId,
        String actorType
) {
}
