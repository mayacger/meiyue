package com.meiyuemall.aftersale.dto;

import java.util.List;

/**
 * 售后单视图（I6 + I29）。
 *
 * @param evidenceImageUrls 凭证图 URL 列表
 */
public record AftersaleResponse(
        Long id,
        String aftersaleNo,
        Long orderId,
        Long orderItemId,
        Long tenantId,
        String type,
        String status,
        String reason,
        long refundCents,
        String sellerDeadlineAt,
        Long reverseShipmentId,
        String reviewNote,
        List<String> evidenceImageUrls
) {}
