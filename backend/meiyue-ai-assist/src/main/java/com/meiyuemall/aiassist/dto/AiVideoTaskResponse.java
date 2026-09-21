package com.meiyuemall.aiassist.dto;

/**
 * AI 推广视频任务响应。
 *
 * @param id             任务 ID
 * @param tenantId       租户
 * @param productId      可选挂接商品
 * @param status         PENDING/RUNNING/SUCCEEDED/FAILED
 * @param prompt         提示词
 * @param resultAssetId  成功后素材 ID
 * @param resultUrl      成功后视频 URL
 * @param failReason     失败原因
 * @param createdAt      创建时间
 */
public record AiVideoTaskResponse(
        Long id,
        Long tenantId,
        Long productId,
        String status,
        String prompt,
        Long resultAssetId,
        String resultUrl,
        String failReason,
        String createdAt
) {}
