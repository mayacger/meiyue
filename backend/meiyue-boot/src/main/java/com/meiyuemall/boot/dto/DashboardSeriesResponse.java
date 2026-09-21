package com.meiyuemall.boot.dto;

import java.util.List;

/**
 * 近 N 日经营/运营序列（I27 图表）。
 *
 * @param days 按日点：date / orderCount / salesCents（Admin 用平台口径）
 */
public record DashboardSeriesResponse(
        List<DayPoint> days
) {
    /**
     * @param date         yyyy-MM-dd
     * @param orderCount   当日已支付订单数
     * @param salesCents   当日销售额（分）
     */
    public record DayPoint(
            String date,
            long orderCount,
            long salesCents
    ) {}
}
