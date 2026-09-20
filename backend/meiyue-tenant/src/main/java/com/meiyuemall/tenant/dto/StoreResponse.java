package com.meiyuemall.tenant.dto;

import java.time.Instant;

/**
 * 店铺视图。
 */
public record StoreResponse(
        Long id,
        Long tenantId,
        String name,
        String slug,
        String status,
        Instant createdAt
) {
}
