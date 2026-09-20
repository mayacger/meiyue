package com.meiyuemall.catalog.dto;

public record CategoryResponse(Long id, Long parentId, String name, int sortOrder) {}
