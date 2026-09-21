package com.meiyuemall.trade.dto;

import java.util.List;

/**
 * 订单视图（I3 + I31 运费 + I32/I33 备注发票）。
 *
 * @param goodsCents    商品应付（券后、运费前）；历史单可空
 * @param freightCents  运费合计
 * @param buyerRemark   买家备注
 * @param invoiceTitle  发票抬头快照
 * @param invoiceTaxNo  税号
 * @param invoiceType   PERSONAL / COMPANY
 * @param deliveredAt   签收时间
 * @param autoConfirmAt 计划自动确认时间
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
        String paymentNo,
        String buyerRemark,
        String invoiceTitle,
        String invoiceTaxNo,
        String invoiceType,
        String deliveredAt,
        String autoConfirmAt
) {}
