package com.meiyuemall.trade.dto;

import java.util.List;

/**
 * 运费预估（I31）。
 *
 * @param goodsCents   商品小计（分）
 * @param freightCents 运费合计
 * @param totalCents   商品+运费（未扣券；结算页扣券后以 checkout 为准）
 * @param shops        按店明细
 */
public record FreightEstimateResponse(
        long goodsCents,
        long freightCents,
        long totalCents,
        List<ShopFreight> shops
) {
    /**
     * @param tenantId       店铺租户
     * @param storeName      店名
     * @param goodsCents     本店商品小计
     * @param freightCents   本店运费
     * @param freeShipping   是否已达包邮
     * @param thresholdCents 包邮门槛（可空）
     */
    public record ShopFreight(
            Long tenantId,
            String storeName,
            long goodsCents,
            long freightCents,
            boolean freeShipping,
            Long thresholdCents
    ) {}
}
