package com.meiyuemall.trade.service;

import com.meiyuemall.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I10/I12：店券与平台券互斥 + 跨店规则单测。
 */
class CouponStackingRulesTest {

    @Test
    void mutualExclusiveRejectsBothClaims() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> CouponStackingRules.assertExclusive(1L, 2L, "MUTUAL_EXCLUSIVE"));
        assertTrue(ex.getMessage().contains("不可同时使用"));
    }

    @Test
    void allowsStoreOnly() {
        assertDoesNotThrow(() -> CouponStackingRules.assertExclusive(1L, null, "MUTUAL_EXCLUSIVE"));
    }

    @Test
    void allowsPlatformOnly() {
        assertDoesNotThrow(() -> CouponStackingRules.assertExclusive(null, 9L, "MUTUAL_EXCLUSIVE"));
    }

    @Test
    void blankModeDefaultsToExclusive() {
        assertThrows(BusinessException.class,
                () -> CouponStackingRules.assertExclusive(1L, 2L, null));
        assertThrows(BusinessException.class,
                () -> CouponStackingRules.assertExclusive(1L, 2L, "  "));
    }

    @Test
    void storeCouponRejectedOnCrossStore() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> CouponStackingRules.assertStoreCouponSingleShop(false));
        assertTrue(ex.getMessage().contains("跨店"));
        assertTrue(ex.getMessage().contains("店券"));
    }

    @Test
    void storeCouponAllowedOnSingleShop() {
        assertDoesNotThrow(() -> CouponStackingRules.assertStoreCouponSingleShop(true));
    }

    @Test
    void platformCouponAllowedOnCrossStore() {
        assertTrue(CouponStackingRules.platformCouponAllowedOnCrossStore());
    }
}
