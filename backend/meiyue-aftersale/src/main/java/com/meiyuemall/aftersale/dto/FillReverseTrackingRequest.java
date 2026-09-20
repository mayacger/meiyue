package com.meiyuemall.aftersale.dto;

import jakarta.validation.constraints.NotBlank;

public record FillReverseTrackingRequest(
        @NotBlank String carrierCode,
        @NotBlank String trackingNo
) {}
