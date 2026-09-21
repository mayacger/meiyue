package com.meiyuemall.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payment_notify_logs")
public class PaymentNotifyLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 32)
    private String channel;
    @Column(name = "channel_trade_no", nullable = false, length = 128)
    private String channelTradeNo;
    @Column(name = "payment_no", length = 32)
    private String paymentNo;
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "verify_result", nullable = false, length = 32)
    private String verifyResult;
    @Column(name = "process_result", nullable = false, length = 32)
    private String processResult;
    @Column(name = "payload_hash", length = 128)
    private String payloadHash;
    @Column(name = "raw_body_preview", length = 1024)
    private String rawBodyPreview;
    @Column(name = "error_message", length = 512)
    private String errorMessage;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setChannel(String channel) { this.channel = channel; }
    public void setChannelTradeNo(String channelTradeNo) { this.channelTradeNo = channelTradeNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setVerifyResult(String verifyResult) { this.verifyResult = verifyResult; }
    public void setProcessResult(String processResult) { this.processResult = processResult; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
    public void setRawBodyPreview(String rawBodyPreview) { this.rawBodyPreview = rawBodyPreview; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
