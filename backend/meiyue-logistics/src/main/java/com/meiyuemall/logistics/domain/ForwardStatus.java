package com.meiyuemall.logistics.domain;

/** 正向物流状态机（规划 C2） */
public enum ForwardStatus {
    PENDING_PICKUP,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    EXCEPTION,
    REJECTED,
    RETURNED_TO_SENDER;

    public boolean canTransitTo(ForwardStatus next) {
        if (this == next) return true;
        return switch (this) {
            case PENDING_PICKUP -> next == PICKED_UP || next == EXCEPTION;
            case PICKED_UP -> next == IN_TRANSIT || next == EXCEPTION || next == RETURNED_TO_SENDER;
            case IN_TRANSIT -> next == OUT_FOR_DELIVERY || next == EXCEPTION || next == RETURNED_TO_SENDER;
            case OUT_FOR_DELIVERY -> next == DELIVERED || next == REJECTED || next == EXCEPTION;
            default -> false;
        };
    }
}
