package com.meiyuemall.tenant.dto;

import java.time.Instant;

/**
 * 店铺视图（I22 + I31 运费）。
 *
 * @param freightCents                 默认运费（分）
 * @param freeShippingThresholdCents   包邮门槛（分，可空）
 */
public record StoreResponse(
        Long id,
        Long tenantId,
        String name,
        String slug,
        String description,
        String logoUrl,
        String status,
        Instant createdAt,
        long freightCents,
        Long freeShippingThresholdCents
) {}
