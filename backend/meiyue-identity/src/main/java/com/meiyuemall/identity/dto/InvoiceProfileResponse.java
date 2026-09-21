package com.meiyuemall.identity.dto;

/**
 * 发票抬头视图（I33）。
 */
public record InvoiceProfileResponse(
        Long id,
        String title,
        String taxNo,
        String invoiceType,
        boolean defaultProfile
) {}
