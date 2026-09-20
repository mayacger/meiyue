package com.meiyuemall.aiassist.dto;

/** 素材响应 */
public record MediaAssetResponse(
        Long id,
        Long tenantId,
        String assetType,
        String source,
        String url,
        String prompt,
        String moderationStatus,
        String moderationNote,
        String failReason,
        /** 失败时提示前端改走人工上传 */
        boolean allowManualUpload
) {}
