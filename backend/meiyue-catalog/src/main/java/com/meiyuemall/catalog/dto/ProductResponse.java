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
        String status,
        List<SkuResponse> skus,
        Instant updatedAt
) {}
