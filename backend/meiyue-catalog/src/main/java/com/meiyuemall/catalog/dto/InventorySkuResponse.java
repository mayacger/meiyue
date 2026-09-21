package com.meiyuemall.catalog.dto;

/**
 * 商家库存行（I20）。
 *
 * @param skuId       SKU 主键
 * @param productId   商品 ID
 * @param productTitle 商品标题
 * @param skuCode     SKU 编码
 * @param specText    规格文案
 * @param priceCents  单价（分）
 * @param stockQty    可售库存
 * @param productStatus 商品状态
 */
public record InventorySkuResponse(
        Long skuId,
        Long productId,
        String productTitle,
        String skuCode,
        String specText,
        long priceCents,
        int stockQty,
        String productStatus
) {}
