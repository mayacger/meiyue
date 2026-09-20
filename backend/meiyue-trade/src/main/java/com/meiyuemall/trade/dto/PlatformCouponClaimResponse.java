package com.meiyuemall.trade.dto;

/** 平台券领取响应 */
public record PlatformCouponClaimResponse(
        Long id,
        Long couponId,
        String status,
        Long orderId,
        Long discountAppliedCents
) {}
