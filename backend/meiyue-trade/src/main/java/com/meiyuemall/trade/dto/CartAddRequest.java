package com.meiyuemall.trade.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartAddRequest(@NotNull Long skuId, @Min(1) int quantity) {}
