package com.meiyuemall.trade.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;

/**
 * 店券与平台券叠加规则（I10 定稿）。
 * <ul>
 *   <li><b>MUTUAL_EXCLUSIVE（默认）</b>：同一订单不可同时使用店券与平台券。</li>
 *   <li>店券：仅单店订单，按该店行小计校验门槛。</li>
 *   <li>平台券：按整单应付小计（扣店券前）校验门槛；互斥模式下与店券二选一。</li>
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
}
