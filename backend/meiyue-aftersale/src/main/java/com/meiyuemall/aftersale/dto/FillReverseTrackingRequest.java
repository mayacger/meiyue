package com.meiyuemall.aftersale.dto;

import jakarta.validation.constraints.NotBlank;

public record FillReverseTrackingRequest(
        @NotBlank String carrierCode,
        @NotBlank String trackingNo,
        /** 可选备注（寄件提示） */
        String remark
) {}
