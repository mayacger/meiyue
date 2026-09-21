package com.meiyuemall.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建/更新 Banner（I28）。
 *
 * @param title     标题
 * @param imageUrl  图片 URL
 * @param linkUrl   跳转（可空）
 * @param sortOrder 排序
 * @param enabled   启用
 * @param startAt   ISO-8601 开始（可空）
 * @param endAt     ISO-8601 结束（可空）
 */
public record BannerUpsertRequest(
        @NotBlank @Size(max = 128) String title,
        @NotBlank @Size(max = 1024) String imageUrl,
        @Size(max = 1024) String linkUrl,
        Integer sortOrder,
        Boolean enabled,
        String startAt,
        String endAt
) {}
