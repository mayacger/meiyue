package com.meiyuemall.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 创建/更新商品请求。
 * <p>{@code coverImageUrl} — I8 人工上传或 AI 通过后的封面；可空，AI 失败时仍可仅填标题上架。</p>
 */
public record ProductUpsertRequest(
        Long categoryId,
        @NotBlank @Size(max = 256) String title,
        @Size(max = 512) String subtitle,
        String detailHtml,
        @Size(max = 1024) String coverImageUrl,
        @NotEmpty @Valid List<SkuRequest> skus
) {
    public record SkuRequest(
            @NotBlank @Size(max = 64) String skuCode,
            @Size(max = 256) String specText,
            @Min(1) long priceCents,
            @Min(0) int stockQty
    ) {}
}
