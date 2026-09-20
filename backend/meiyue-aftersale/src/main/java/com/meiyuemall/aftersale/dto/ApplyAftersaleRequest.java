package com.meiyuemall.aftersale.dto;

import com.meiyuemall.aftersale.domain.AftersaleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplyAftersaleRequest(
        @NotNull Long orderId,
        Long orderItemId,
        @NotNull AftersaleType type,
        @NotBlank String reason,
        @Min(1) long refundCents
) {}
