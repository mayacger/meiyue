package com.meiyuemall.boot.dto;

/**
 * 平台运营概览（I20）。
 *
 * @param pendingOnboardingCount 待审入驻
 * @param buyerUserCount         买家角色账号数
 * @param sellerUserCount        店主角色账号数
 * @param enabledCategoryCount   启用类目数
 * @param platformCouponCount    平台券数
 * @param onSaleProductCount     在售商品数
 */
public record AdminDashboardResponse(
        long pendingOnboardingCount,
        long buyerUserCount,
        long sellerUserCount,
        long enabledCategoryCount,
        long platformCouponCount,
        long onSaleProductCount
) {}
