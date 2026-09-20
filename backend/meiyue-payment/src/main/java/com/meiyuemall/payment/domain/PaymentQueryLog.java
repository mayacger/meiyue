package com.meiyuemall.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payment_query_logs")
public class PaymentQueryLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "payment_no", nullable = false, length = 32)
    private String paymentNo;
    @Column(nullable = false, length = 32)
    private String channel;
    @Column(name = "query_result", nullable = false, length = 32)
    private String queryResult;
    @Column(name = "channel_trade_no", length = 128)
    private String channelTradeNo;
    @Column(name = "raw_preview", length = 1024)
    private String rawPreview;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public void setChannel(String channel) { this.channel = channel; }
    public void setQueryResult(String queryResult) { this.queryResult = queryResult; }
    public void setChannelTradeNo(String channelTradeNo) { this.channelTradeNo = channelTradeNo; }
    public void setRawPreview(String rawPreview) { this.rawPreview = rawPreview; }
}
