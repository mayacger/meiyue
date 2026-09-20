package com.meiyuemall.payment.dto;

/** 周期汇总：账期 + 状态下的金额合计与笔数 */
public record SettlementPeriodSummary(
        String periodKey,
        String status,
        long amountCents,
        long entryCount
) {}
