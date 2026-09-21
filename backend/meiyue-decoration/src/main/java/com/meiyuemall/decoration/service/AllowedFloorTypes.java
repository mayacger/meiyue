package com.meiyuemall.decoration.service;

/**
 * 允许的装修楼层类型（I10 增强）。禁止 LIVE / LIVE_STREAM。
 */
public final class AllowedFloorTypes {
    private AllowedFloorTypes() {}

    public static final java.util.Set<String> ALLOWED = java.util.Set.of(
            "BANNER",
            "PRODUCT_RECOMMEND",
            "PRODUCT_GROUP",
            "IMAGE_TEXT",
            "IMAGE_STRIP",
            "RICH_TEXT",
            "CATEGORY_NAV",
            "COUPON_ENTRY"
    );
}
