package com.meiyuemall.trade.dto;

/**
 * 商品评价视图（I10 + I30 hidden）。
 *
 * @param hidden       是否被平台隐藏
 * @param hiddenReason 隐藏原因（Admin/Seller 可见）
 */
public record ProductReviewResponse(
        Long id,
        Long productId,
        Long orderId,
        Long orderItemId,
        Long tenantId,
        int rating,
        String content,
        String sellerReply,
        String createdAt,
        boolean hidden,
        String hiddenReason
) {}
