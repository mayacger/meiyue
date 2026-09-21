package com.meiyuemall.trade.dto;

public record CouponClaimResponse(
        Long id,
        Long couponId,
        Long tenantId,
        String status,
        Long orderId,
        Long discountAppliedCents
) {}
