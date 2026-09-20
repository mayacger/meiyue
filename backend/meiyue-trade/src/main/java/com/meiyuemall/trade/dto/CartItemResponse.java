package com.meiyuemall.trade.dto;

public record CartItemResponse(
        Long id,
        Long tenantId,
        Long productId,
        Long skuId,
        String productTitle,
        String skuCode,
        long unitPriceCents,
        int quantity,
        long lineTotalCents
) {}
