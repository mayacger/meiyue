package com.meiyuemall.tenant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 商家店铺设置更新（I22 + I31 运费模板）。
 *
 * @param name                         店名
 * @param description                  简介
 * @param logoUrl                      Logo URL
 * @param freightCents                 默认运费（分）
 * @param freeShippingThresholdCents   包邮门槛（分）；传负数表示清空为无包邮
 */
public record StoreUpdateRequest(
        @Size(max = 128) String name,
        @Size(max = 512) String description,
        @Size(max = 1024) String logoUrl,
        @Min(0) Long freightCents,
        Long freeShippingThresholdCents
) {}
