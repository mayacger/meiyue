package com.meiyuemall.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 发票抬头创建/更新（I33）。
 *
 * @param title          抬头名称
 * @param taxNo          税号（企业建议填）
 * @param invoiceType    PERSONAL / COMPANY
 * @param defaultProfile 是否默认
 */
public record InvoiceProfileUpsertRequest(
        @NotBlank @Size(max = 128) String title,
        @Size(max = 64) String taxNo,
        @Size(max = 16) String invoiceType,
        Boolean defaultProfile
) {}
