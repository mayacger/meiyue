package com.meiyuemall.trade.dto;

import java.util.List;

/**
 * 下单请求。
 * <p>券规则：店券 {@code storeCouponClaimId} 与平台券 {@code platformCouponClaimId}
 * 默认<strong>互斥</strong>（不可同时传），见 {@code CouponStackingRules}。</p>
 *
 * @param cartItemIds           可选购物车行
 * @param storeCouponClaimId    店券领取 ID（兼容旧字段 couponClaimId）
 * @param platformCouponClaimId 平台券领取 ID
 * @param couponClaimId         兼容旧客户端：等同 storeCouponClaimId
 */
public record CheckoutRequest(
        List<Long> cartItemIds,
        Long storeCouponClaimId,
        Long platformCouponClaimId,
        Long couponClaimId
) {
    /** 解析实际店券 claim：优先 storeCouponClaimId，否则旧字段 couponClaimId */
    public Long resolvedStoreClaimId() {
        return storeCouponClaimId != null ? storeCouponClaimId : couponClaimId;
    }
}
