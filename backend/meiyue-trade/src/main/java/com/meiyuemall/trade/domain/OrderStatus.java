package com.meiyuemall.trade.domain;

/** 订单状态：pending_payment → paid → fulfilling → completed | cancelled */
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    FULFILLING,
    COMPLETED,
    CANCELLED
}
