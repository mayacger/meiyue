package com.meiyuemall.catalog.dto;

import com.meiyuemall.catalog.domain.CategoryStatus;

/**
 * 类目响应。
 *
 * @param id        主键
 * @param parentId  父类目，根为 null
 * @param name      名称
 * @param sortOrder 排序（越小越前）
 * @param status    ENABLED / DISABLED
 */
public record CategoryResponse(
        Long id,
        Long parentId,
        String name,
        int sortOrder,
        CategoryStatus status
) {}
