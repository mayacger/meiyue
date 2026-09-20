package com.meiyuemall.trade.dto;

public record OrderItemResponse(
        Long id,
        Long tenantId,
        Long productId,
        Long skuId,
        String productTitle,
        String skuCode,
        String specText,
        long unitPriceCents,
        int quantity,
        long lineTotalCents
) {}
