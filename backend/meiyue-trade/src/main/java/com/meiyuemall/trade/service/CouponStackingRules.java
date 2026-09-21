package com.meiyuemall.trade.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;

/**
 * 店券与平台券规则（I10/I12）。
 * <ul>
 *   <li><b>MUTUAL_EXCLUSIVE（默认）</b>：同一订单不可同时使用店券与平台券。</li>
 *   <li><b>店券</b>：仅<strong>单店</strong>订单可用；按该店行小计校验门槛。跨店订单禁止店券。</li>
 *   <li><b>平台券</b>：按整单小计校验门槛；<strong>可用于跨店订单</strong>（整单抵扣）。</li>
 * </ul>
 * 配置：{@code meiyue.coupon.stacking=MUTUAL_EXCLUSIVE}（预留 STACKABLE，本迭代不启用）。
 */
public final class CouponStackingRules {

    public static final String MODE_MUTUAL_EXCLUSIVE = "MUTUAL_EXCLUSIVE";

    private CouponStackingRules() {
    }

    public static void assertExclusive(Long storeClaimId, Long platformClaimId, String mode) {
        String m = mode == null || mode.isBlank() ? MODE_MUTUAL_EXCLUSIVE : mode.trim().toUpperCase();
        if (MODE_MUTUAL_EXCLUSIVE.equals(m)
                && storeClaimId != null
                && platformClaimId != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "店券与平台券不可同时使用（规则：MUTUAL_EXCLUSIVE）");
        }
    }

    /**
     * I12：店券仅单店。跨店订单请只用平台券或分店结算。
     *
     * @param singleTenant 订单是否仅含一个租户
     */
    public static void assertStoreCouponSingleShop(boolean singleTenant) {
        if (!singleTenant) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "跨店订单不支持店券（仅平台券可用于跨店）；请分店结算或改用平台券");
        }
    }

    /**
     * 平台券是否允许用于跨店：始终允许（文档化断言，便于单测对齐产品规则）。
     */
    public static boolean platformCouponAllowedOnCrossStore() {
        return true;
    }
}
