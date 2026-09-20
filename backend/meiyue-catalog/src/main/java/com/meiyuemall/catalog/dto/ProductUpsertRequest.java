package com.meiyuemall.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 创建/更新商品请求 */
public record ProductUpsertRequest(
        Long categoryId,
        @NotBlank @Size(max = 256) String title,
        @Size(max = 512) String subtitle,
        String detailHtml,
        @NotEmpty @Valid List<SkuRequest> skus
) {
    public record SkuRequest(
            @NotBlank @Size(max = 64) String skuCode,
            @Size(max = 256) String specText,
            @Min(1) long priceCents,
            @Min(0) int stockQty
    ) {}
}
