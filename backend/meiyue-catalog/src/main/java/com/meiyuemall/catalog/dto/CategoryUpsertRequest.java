package com.meiyuemall.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 平台类目创建/更新请求。
 *
 * @param parentId  父类目 ID，可空（根类目）
 * @param name      类目名
 * @param sortOrder 排序，可空默认 0
 */
public record CategoryUpsertRequest(
        Long parentId,
        @NotBlank @Size(max = 128) String name,
        Integer sortOrder
) {}
