package com.meiyuemall.trade.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull Long orderId,
        @NotNull Long orderItemId,
        @Min(1) @Max(5) int rating,
        @NotBlank @Size(max = 1000) String content
) {}
