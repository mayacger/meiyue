package com.meiyuemall.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 创建/更新商品请求（I8 封面 + I34 图集/推广视频 URL）。
 *
 * @param coverImageUrl     封面
 * @param galleryImageUrls  图集 URL 列表（可空）
 * @param promoVideoUrl     推广视频 URL（非直播，可空）
 */
public record ProductUpsertRequest(
        Long categoryId,
        @NotBlank @Size(max = 256) String title,
        @Size(max = 512) String subtitle,
        String detailHtml,
        @Size(max = 1024) String coverImageUrl,
        List<@Size(max = 1024) String> galleryImageUrls,
        @Size(max = 1024) String promoVideoUrl,
        @NotEmpty @Valid List<SkuRequest> skus
) {
    public record SkuRequest(
            @NotBlank @Size(max = 64) String skuCode,
            @Size(max = 256) String specText,
            @Min(1) long priceCents,
            @Min(0) int stockQty
    ) {}
}
