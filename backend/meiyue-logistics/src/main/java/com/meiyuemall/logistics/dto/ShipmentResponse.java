package com.meiyuemall.logistics.dto;

import java.util.List;

public record ShipmentResponse(
        Long id,
        Long tenantId,
        Long orderId,
        Long orderItemId,
        String direction,
        String carrierCode,
        String trackingNo,
        String status,
        Long aftersaleId,
        List<ShipmentTrackResponse> tracks
) {}
