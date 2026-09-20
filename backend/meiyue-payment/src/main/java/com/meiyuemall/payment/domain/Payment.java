package com.meiyuemall.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 支付单实体。
 * <p>字段说明：paymentNo 平台支付号；channel 通道；channelTradeNo 通道流水（幂等/对账键）；
 * idempotentKey 创建时业务幂等键；notifyCount 回调次数；lastQueryAt 最近查单时间。</p>
 */
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "payment_no", nullable = false, unique = true, length = 32)
    private String paymentNo;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentChannel channel;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status = PaymentStatus.PENDING;
    @Column(name = "amount_cents", nullable = false)
    private long amountCents;
    @Column(name = "channel_trade_no", length = 64)
    private String channelTradeNo;
    @Column(name = "idempotent_key", length = 64)
    private String idempotentKey;
    @Column(name = "notify_count", nullable = false)
    private int notifyCount = 0;
    @Column(name = "last_query_at")
    private Instant lastQueryAt;
    @Column(name = "paid_at")
    private Instant paidAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public PaymentChannel getChannel() { return channel; }
    public void setChannel(PaymentChannel channel) { this.channel = channel; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public String getChannelTradeNo() { return channelTradeNo; }
    public void setChannelTradeNo(String channelTradeNo) { this.channelTradeNo = channelTradeNo; }
    public String getIdempotentKey() { return idempotentKey; }
    public void setIdempotentKey(String idempotentKey) { this.idempotentKey = idempotentKey; }
    public int getNotifyCount() { return notifyCount; }
    public void setNotifyCount(int notifyCount) { this.notifyCount = notifyCount; }
    public Instant getLastQueryAt() { return lastQueryAt; }
    public void setLastQueryAt(Instant lastQueryAt) { this.lastQueryAt = lastQueryAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}
