package com.meiyuemall.platform.dto;

import java.util.List;

/**
 * 平台运营配置只读视图（I23）。
 *
 * @param items                 全部键值项
 * @param platformFeeRateBps    平台服务费率（基点，50=0.5%）
 * @param settlementCycle       结算周期 WEEKLY / MONTHLY
 * @param minWithdrawCents      最低提现门槛（分）
 */
public record PlatformConfigResponse(
        List<PlatformConfigItem> items,
        String platformFeeRateBps,
        String settlementCycle,
        String minWithdrawCents
) {
    /**
     * 单项配置。
     *
     * @param key         键
     * @param value       值
     * @param description 说明
     * @param updatedAt   更新时间 ISO
     */
    public record PlatformConfigItem(
            String key,
            String value,
            String description,
            String updatedAt
    ) {}
}
