package com.meiyuemall.catalog.dto;

import java.time.Instant;
import java.util.List;

public record ProductResponse(
        Long id,
        Long tenantId,
        Long categoryId,
        String title,
        String subtitle,
        String detailHtml,
        String coverImageUrl,
        Long coverAssetId,
        /** I11 推广视频 URL（非直播） */
        String promoVideoUrl,
        Long promoVideoAssetId,
        String status,
        List<SkuResponse> skus,
        Instant updatedAt
) {}
