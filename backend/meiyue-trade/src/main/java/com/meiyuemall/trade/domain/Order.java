package com.meiyuemall.trade.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;
    @Column(name = "buyer_user_id", nullable = false)
    private Long buyerUserId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;
    @Column(name = "total_cents", nullable = false)
    private long totalCents;
    /** I31：商品应付（券后、运费前） */
    @Column(name = "goods_cents")
    private Long goodsCents;
    /** I31：运费合计 */
    @Column(name = "freight_cents", nullable = false)
    private long freightCents = 0;
    @Column(name = "pay_expire_at", nullable = false)
    private Instant payExpireAt;
    @Column(name = "paid_at")
    private Instant paidAt;
    @Column(name = "cancelled_at")
    private Instant cancelledAt;
    @Column(name = "cancel_reason", length = 128)
    private String cancelReason;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now; updatedAt = now;
    }
    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getBuyerUserId() { return buyerUserId; }
    public void setBuyerUserId(Long buyerUserId) { this.buyerUserId = buyerUserId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public long getTotalCents() { return totalCents; }
    public void setTotalCents(long totalCents) { this.totalCents = totalCents; }
    public Long getGoodsCents() { return goodsCents; }
    public void setGoodsCents(Long goodsCents) { this.goodsCents = goodsCents; }
    public long getFreightCents() { return freightCents; }
    public void setFreightCents(long freightCents) { this.freightCents = freightCents; }
    public Instant getPayExpireAt() { return payExpireAt; }
    public void setPayExpireAt(Instant payExpireAt) { this.payExpireAt = payExpireAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}
