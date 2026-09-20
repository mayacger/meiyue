package com.meiyuemall.logistics.domain;

/** 逆向物流状态机（规划 C3） */
public enum ReverseStatus {
    PENDING_SEND,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED_TO_SELLER,
    EXCEPTION;

    public boolean canTransitTo(ReverseStatus next) {
        if (this == next) return true;
        return switch (this) {
            case PENDING_SEND -> next == PICKED_UP || next == EXCEPTION;
            case PICKED_UP -> next == IN_TRANSIT || next == EXCEPTION;
            case IN_TRANSIT -> next == OUT_FOR_DELIVERY || next == EXCEPTION;
            case OUT_FOR_DELIVERY -> next == DELIVERED_TO_SELLER || next == EXCEPTION;
            default -> false;
        };
    }
}
