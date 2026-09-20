package com.meiyuemall.aftersale.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "aftersales")
public class Aftersale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "aftersale_no", nullable = false, unique = true, length = 32)
    private String aftersaleNo;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "order_item_id")
    private Long orderItemId;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "buyer_user_id", nullable = false)
    private Long buyerUserId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AftersaleType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AftersaleStatus status;
    @Column(nullable = false, length = 512)
    private String reason;
    @Column(name = "refund_cents", nullable = false)
    private long refundCents;
    @Column(name = "seller_deadline_at", nullable = false)
    private Instant sellerDeadlineAt;
    @Column(name = "reviewed_at")
    private Instant reviewedAt;
    @Column(name = "review_note", length = 512)
    private String reviewNote;
    @Column(name = "reverse_shipment_id")
    private Long reverseShipmentId;
    @Column(name = "closed_at")
    private Instant closedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist void onCreate() { Instant n=Instant.now(); createdAt=n; updatedAt=n; }
    @PreUpdate void onUpdate() { updatedAt=Instant.now(); }

    public Long getId() { return id; }
    public String getAftersaleNo() { return aftersaleNo; }
    public void setAftersaleNo(String aftersaleNo) { this.aftersaleNo = aftersaleNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getBuyerUserId() { return buyerUserId; }
    public void setBuyerUserId(Long buyerUserId) { this.buyerUserId = buyerUserId; }
    public AftersaleType getType() { return type; }
    public void setType(AftersaleType type) { this.type = type; }
    public AftersaleStatus getStatus() { return status; }
    public void setStatus(AftersaleStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public long getRefundCents() { return refundCents; }
    public void setRefundCents(long refundCents) { this.refundCents = refundCents; }
    public Instant getSellerDeadlineAt() { return sellerDeadlineAt; }
    public void setSellerDeadlineAt(Instant sellerDeadlineAt) { this.sellerDeadlineAt = sellerDeadlineAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public Long getReverseShipmentId() { return reverseShipmentId; }
    public void setReverseShipmentId(Long reverseShipmentId) { this.reverseShipmentId = reverseShipmentId; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
