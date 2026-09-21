package com.meiyuemall.boot.dto;

/**
 * 商家经营概览（I20）。
 *
 * @param pendingShipCount     待发货订单数（PAID）
 * @param pendingAftersaleCount 待处理售后数（APPLIED/REVIEWING）
 * @param todayOrderCount      今日已支付订单数
 * @param todaySalesCents      今日销售额（分，本店行合计）
 * @param lowStockSkuCount     库存≤5 的 SKU 数
 */
public record SellerDashboardResponse(
        long pendingShipCount,
        long pendingAftersaleCount,
        long todayOrderCount,
        long todaySalesCents,
        long lowStockSkuCount
) {}
