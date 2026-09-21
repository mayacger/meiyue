package com.meiyuemall.payment.dto;

public record PaymentResponse(
        Long id,
        String paymentNo,
        Long orderId,
        String channel,
        String status,
        long amountCents,
        String channelTradeNo,
        String paidAt
) {}
