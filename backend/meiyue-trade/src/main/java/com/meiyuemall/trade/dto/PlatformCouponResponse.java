package com.meiyuemall.trade.dto;

/** 平台券响应 */
public record PlatformCouponResponse(
        Long id,
        String code,
        String title,
        long discountCents,
        long minSpendCents,
        int totalQuota,
        int claimedCount,
        String status
) {}
