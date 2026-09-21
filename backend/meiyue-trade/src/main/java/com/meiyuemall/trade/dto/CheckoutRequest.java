package com.meiyuemall.trade.dto;

import java.util.List;

/**
 * 下单请求（I3 + I9 券 + I33 备注/发票）。
 * <p>券规则：店券与平台券默认互斥，见 {@code CouponStackingRules}。</p>
 *
 * @param cartItemIds           可选购物车行
 * @param storeCouponClaimId    店券领取 ID
 * @param platformCouponClaimId 平台券领取 ID
 * @param couponClaimId         兼容旧客户端：等同 storeCouponClaimId
 * @param buyerRemark           买家备注（I33）
 * @param invoiceTitle          发票抬头（可由前端从抬头库带入）
 * @param invoiceTaxNo          税号
 * @param invoiceType           PERSONAL / COMPANY
 */
public record CheckoutRequest(
        List<Long> cartItemIds,
        Long storeCouponClaimId,
        Long platformCouponClaimId,
        Long couponClaimId,
        String buyerRemark,
        String invoiceTitle,
        String invoiceTaxNo,
        String invoiceType
) {
    /** 解析实际店券 claim：优先 storeCouponClaimId，否则旧字段 couponClaimId */
    public Long resolvedStoreClaimId() {
        return storeCouponClaimId != null ? storeCouponClaimId : couponClaimId;
    }
}
