package com.meiyuemall.support.dto;

/**
 * 站内通知响应。
 *
 * @param id       主键
 * @param audience 受众 BUYER/SELLER/PLATFORM
 * @param title    标题
 * @param body     正文
 * @param category 分类
 * @param refType  引用类型
 * @param refId    引用 ID
 * @param read     是否已读
 * @param createdAt 创建时间 ISO-8601
 */
public record NotificationResponse(
        Long id,
        String audience,
        String title,
        String body,
        String category,
        String refType,
        String refId,
        boolean read,
        String createdAt
) {}
