package com.meiyuemall.catalog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 批量上下架请求（I25）。
 *
 * @param productIds 本店商品 ID 列表
 * @param status     ON_SALE / OFF_SALE / DRAFT
 */
public record BatchProductStatusRequest(
        @NotEmpty List<Long> productIds,
        @NotNull String status
) {}
