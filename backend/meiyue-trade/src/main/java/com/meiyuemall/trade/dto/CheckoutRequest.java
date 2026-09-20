package com.meiyuemall.trade.dto;

import java.util.List;

public record CheckoutRequest(List<Long> cartItemIds) {}
