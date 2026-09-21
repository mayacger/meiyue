package com.meiyuemall.tenant.dto;

/**
 * 商家成员视图（I26）。
 *
 * @param id         seller_members 主键
 * @param userId     用户 ID
 * @param username   登录名
 * @param displayName 展示名
 * @param memberRole OWNER / STAFF
 * @param createdAt  加入时间
 */
public record StaffMemberResponse(
        Long id,
        Long userId,
        String username,
        String displayName,
        String memberRole,
        String createdAt
) {}
