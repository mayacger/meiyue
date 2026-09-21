package com.meiyuemall.trade.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 商家创建店券 */
public record CreateStoreCouponRequest(
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 128) String title,
        @Min(1) long discountCents,
        @Min(0) long minSpendCents,
        @Min(0) int totalQuota
) {}
