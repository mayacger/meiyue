package com.meiyuemall.logistics.dto;

import java.util.List;

/**
 * 运单响应（含多包裹序号与电子面单字段）。
 */
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
        int packageSeq,
        String ewaybillNo,
        String ewaybillLabelUrl,
        String ewaybillProvider,
        List<ShipmentTrackResponse> tracks
) {}
