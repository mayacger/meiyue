package com.meiyuemall.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * SKU 库存调整请求（I20）。
 *
 * @param stockQty 目标库存（绝对值，≥0）
 */
public record StockAdjustRequest(
        @NotNull @Min(0) Integer stockQty
) {}
