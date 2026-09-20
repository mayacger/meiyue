package com.meiyuemall.aiassist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 提交 AI 推广视频任务。
 *
 * @param prompt    提示词
 * @param productId 可选：完成后挂到商品 promoVideo
 */
public record GenerateVideoRequest(
        @NotBlank @Size(max = 1024) String prompt,
        Long productId
) {}
