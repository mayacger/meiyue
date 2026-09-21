package com.meiyuemall.platform.dto;

/**
 * Banner 视图（I28）。
 *
 * @param id        主键
 * @param title     标题 / alt
 * @param imageUrl  图片 URL
 * @param linkUrl   跳转
 * @param sortOrder 排序（大者优先）
 * @param enabled   是否启用
 * @param startAt   投放开始（可空）
 * @param endAt     投放结束（可空）
 */
public record BannerResponse(
        Long id,
        String title,
        String imageUrl,
        String linkUrl,
        int sortOrder,
        boolean enabled,
        String startAt,
        String endAt
) {}
