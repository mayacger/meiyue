package com.meiyuemall.catalog.dto;

/**
 * 收藏项响应。
 *
 * @param id        收藏主键
 * @param productId 商品 ID
 * @param createdAt 收藏时间
 */
public record FavoriteResponse(
        Long id,
        Long productId,
        String createdAt
) {}
