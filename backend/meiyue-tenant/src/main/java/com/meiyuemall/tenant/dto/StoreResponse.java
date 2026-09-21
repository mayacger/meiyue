package com.meiyuemall.tenant.dto;

import java.time.Instant;

/**
 * 店铺视图（I22 含简介与 Logo）。
 *
 * @param id          主键
 * @param tenantId    租户
 * @param name        店名
 * @param slug        短链
 * @param description 简介
 * @param logoUrl     Logo URL
 * @param status      OPEN / CLOSED
 * @param createdAt   创建时间
 */
public record StoreResponse(
        Long id,
        Long tenantId,
        String name,
        String slug,
        String description,
        String logoUrl,
        String status,
        Instant createdAt
) {}
