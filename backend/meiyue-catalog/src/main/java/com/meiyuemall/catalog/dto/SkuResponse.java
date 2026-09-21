package com.meiyuemall.catalog.dto;

public record SkuResponse(Long id, String skuCode, String specText, long priceCents, int stockQty) {}
