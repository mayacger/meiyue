package com.meiyuemall.trade.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** 平台券领取记录 */
@Entity
@Table(name = "platform_coupon_claims")
public class PlatformCouponClaim {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "coupon_id", nullable = false)
    private Long couponId;
    @Column(name = "buyer_user_id", nullable = false)
    private Long buyerUserId;
    @Column(nullable = false, length = 16)
    private String status = "CLAIMED";
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "discount_applied_cents")
    private Long discountAppliedCents;
    @Column(name = "claimed_at", nullable = false)
    private Instant claimedAt = Instant.now();
    @Column(name = "used_at")
    private Instant usedAt;

    public Long getId() { return id; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public Long getBuyerUserId() { return buyerUserId; }
    public void setBuyerUserId(Long buyerUserId) { this.buyerUserId = buyerUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getDiscountAppliedCents() { return discountAppliedCents; }
    public void setDiscountAppliedCents(Long discountAppliedCents) { this.discountAppliedCents = discountAppliedCents; }
    public Instant getClaimedAt() { return claimedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
}
