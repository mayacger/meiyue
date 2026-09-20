package com.meiyuemall.trade.dto;

public record StoreCouponResponse(
        Long id,
        Long tenantId,
        String code,
        String title,
        long discountCents,
        long minSpendCents,
        int totalQuota,
        int claimedCount,
        String status
) {}
