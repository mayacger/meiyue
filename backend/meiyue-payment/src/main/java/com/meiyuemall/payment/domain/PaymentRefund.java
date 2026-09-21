package com.meiyuemall.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 通道退款记录（I12）。
 * <p>按 aftersaleId 唯一，保证售后入账触发退款幂等。</p>
 */
@Entity
@Table(name = "payment_refunds")
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_no", nullable = false, length = 32)
    private String paymentNo;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "aftersale_id", nullable = false, unique = true)
    private Long aftersaleId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(nullable = false, length = 32)
    private String channel;

    @Column(name = "channel_refund_no", length = 128)
    private String channelRefundNo;

    /** SUCCESS / FAILED */
    @Column(nullable = false, length = 16)
    private String status;

    @Column(length = 512)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getAftersaleId() { return aftersaleId; }
    public void setAftersaleId(Long aftersaleId) { this.aftersaleId = aftersaleId; }
    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getChannelRefundNo() { return channelRefundNo; }
    public void setChannelRefundNo(String channelRefundNo) { this.channelRefundNo = channelRefundNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getCreatedAt() { return createdAt; }
}
