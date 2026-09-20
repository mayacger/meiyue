package com.meiyuemall.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateForwardShipmentRequest(
        @NotNull Long orderId,
        Long orderItemId,
        @NotBlank String carrierCode,
        @NotBlank String trackingNo,
        String receiverName,
        String receiverPhone,
        String receiverAddress
) {}
