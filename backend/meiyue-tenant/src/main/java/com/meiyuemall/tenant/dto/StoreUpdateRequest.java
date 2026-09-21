package com.meiyuemall.tenant.dto;

import jakarta.validation.constraints.Size;

/**
 * 商家店铺设置更新（I22）。
 *
 * @param name        店名，可空表示不改
 * @param description 简介
 * @param logoUrl     Logo URL（占位）
 */
public record StoreUpdateRequest(
        @Size(max = 128) String name,
        @Size(max = 512) String description,
        @Size(max = 1024) String logoUrl
) {}
