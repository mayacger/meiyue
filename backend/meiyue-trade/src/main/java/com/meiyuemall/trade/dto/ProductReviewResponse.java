package com.meiyuemall.trade.dto;

public record ProductReviewResponse(
        Long id,
        Long productId,
        Long orderId,
        Long orderItemId,
        Long tenantId,
        int rating,
        String content,
        String sellerReply,
        String createdAt
) {}
