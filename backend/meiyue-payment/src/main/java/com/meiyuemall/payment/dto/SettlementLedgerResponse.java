package com.meiyuemall.payment.dto;

/** 结算账本行响应 */
public record SettlementLedgerResponse(
        Long id,
        Long orderId,
        Long orderItemId,
        String entryType,
        long amountCents,
        String status,
        String periodKey,
        String remark,
        String createdAt
) {}
