package com.meiyuemall.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payment_reconcile_diffs")
public class PaymentReconcileDiff {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "batch_id", nullable = false)
    private Long batchId;
    @Column(name = "diff_type", nullable = false, length = 32)
    private String diffType;
    @Column(name = "payment_no", length = 32)
    private String paymentNo;
    @Column(name = "channel_trade_no", length = 128)
    private String channelTradeNo;
    @Column(name = "local_amount_cents")
    private Long localAmountCents;
    @Column(name = "channel_amount_cents")
    private Long channelAmountCents;
    @Column(length = 512)
    private String detail;
    @Column(nullable = false, length = 32)
    private String status = "OPEN";
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public void setDiffType(String diffType) { this.diffType = diffType; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public void setDetail(String detail) { this.detail = detail; }
    public void setStatus(String status) { this.status = status; }
}
