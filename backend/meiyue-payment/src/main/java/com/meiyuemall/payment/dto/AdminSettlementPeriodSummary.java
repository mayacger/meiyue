package com.meiyuemall.payment.dto;

/**
 * 平台结算周期汇总（I28 Admin 只读）。
 *
 * @param periodKey   账期 yyyy-Wnn
 * @param status      PENDING / SETTLED / …
 * @param amountCents 金额合计（分）
 * @param entryCount  账本行数
 * @param tenantCount 涉及商家数
 */
public record AdminSettlementPeriodSummary(
        String periodKey,
        String status,
        long amountCents,
        long entryCount,
        long tenantCount
) {}
