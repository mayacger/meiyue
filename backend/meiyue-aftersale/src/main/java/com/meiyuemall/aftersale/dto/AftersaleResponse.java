package com.meiyuemall.aftersale.dto;

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
        String reviewNote
) {}
