package com.meiyuemall.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 买家创建工单请求。
 *
 * @param subject  标题
 * @param body     正文
 * @param category GENERAL / ORDER / AFTERSALE / PRODUCT
 * @param tenantId 可选商家租户；空=平台工单
 */
public record CreateTicketRequest(
        @NotBlank @Size(max = 128) String subject,
        @NotBlank @Size(max = 2000) String body,
        @Size(max = 32) String category,
        Long tenantId
) {}
