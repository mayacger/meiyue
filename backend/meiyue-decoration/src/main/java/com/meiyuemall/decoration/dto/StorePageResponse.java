package com.meiyuemall.decoration.dto;

public record StorePageResponse(
        Long id,
        Long tenantId,
        Long storeId,
        String templateCode,
        String themeColor,
        String floorsJson,
        String status,
        String publishedAt
) {}
