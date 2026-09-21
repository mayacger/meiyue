package com.meiyuemall.payment.dto;

/**
 * 退款结果响应。
 *
 * @param paymentNo       原支付单号
 * @param aftersaleId     售后 ID
 * @param amountCents     退款金额
 * @param channel         通道
 * @param channelRefundNo 通道退款流水
 * @param status          SUCCESS / FAILED
 * @param duplicate       是否幂等命中（已退过）
 */
public record RefundResponse(
        String paymentNo,
        Long aftersaleId,
        long amountCents,
        String channel,
        String channelRefundNo,
        String status,
        boolean duplicate
) {}
