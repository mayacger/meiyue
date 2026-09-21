package com.meiyuemall.trade.dto;

import java.util.List;

/**
 * 订单视图（I3 + I31 运费）。
 *
 * @param goodsCents   商品应付（券后、运费前）；历史单可空
 * @param freightCents 运费合计
 */
public record OrderResponse(
        Long id,
        String orderNo,
        String status,
        long totalCents,
        Long goodsCents,
        long freightCents,
        String payExpireAt,
        String paidAt,
        List<OrderItemResponse> items,
        String paymentNo
) {}
