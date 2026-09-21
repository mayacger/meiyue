package com.meiyuemall.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建站内通知（管理端或内部调用骨架）。
 *
 * @param userId   接收人用户 ID
 * @param audience BUYER / SELLER / PLATFORM
 * @param title    标题
 * @param body     正文
 * @param category ORDER / AFTERSALE / COUPON / SYSTEM / REVIEW
 * @param refType  可选引用类型
 * @param refId    可选引用 ID
 */
public record CreateNotificationRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 16) String audience,
        @NotBlank @Size(max = 128) String title,
        @NotBlank @Size(max = 512) String body,
        @Size(max = 32) String category,
        @Size(max = 32) String refType,
        @Size(max = 64) String refId
) {}
