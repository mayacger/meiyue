package com.meiyuemall.payment.dto;

/** 结算账本行响应（I28：含 tenantId 供 Admin 汇总） */
public record SettlementLedgerResponse(
        Long id,
        Long tenantId,
        Long orderId,
        Long orderItemId,
        String entryType,
        long amountCents,
        String status,
        String periodKey,
        String remark,
        String createdAt
) {}
