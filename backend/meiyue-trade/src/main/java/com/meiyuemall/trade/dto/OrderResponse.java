package com.meiyuemall.trade.dto;

import java.util.List;

public record OrderResponse(
        Long id,
        String orderNo,
        String status,
        long totalCents,
        String payExpireAt,
        String paidAt,
        List<OrderItemResponse> items,
        String paymentNo
) {}
