package com.meiyuemall.trade.dto;

import java.util.List;

/**
 * 下单请求。
 * @param cartItemIds 可选，指定购物车行；空则全选
 * @param couponClaimId 可选，已领店券 ID（抵扣）
 */
public record CheckoutRequest(List<Long> cartItemIds, Long couponClaimId) {}
