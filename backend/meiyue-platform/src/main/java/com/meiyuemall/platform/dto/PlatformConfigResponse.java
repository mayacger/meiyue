package com.meiyuemall.platform.dto;

import java.util.List;

/**
 * 平台运营配置视图（I23 + I32）。
 *
 * @param items                   全部键值项
 * @param platformFeeRateBps      平台服务费率（基点）
 * @param settlementCycle         结算周期
 * @param minWithdrawCents        最低提现门槛（分）
 * @param autoConfirmReceiptDays  签收后自动确认天数
 */
public record PlatformConfigResponse(
        List<PlatformConfigItem> items,
        String platformFeeRateBps,
        String settlementCycle,
        String minWithdrawCents,
        String autoConfirmReceiptDays
) {
    public record PlatformConfigItem(
            String key,
            String value,
            String description,
            String updatedAt
    ) {}
}
