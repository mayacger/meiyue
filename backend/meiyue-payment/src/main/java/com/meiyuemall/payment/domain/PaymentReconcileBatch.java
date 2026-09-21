package com.meiyuemall.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payment_reconcile_batches")
public class PaymentReconcileBatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "biz_date", nullable = false)
    private LocalDate bizDate;
    @Column(nullable = false, length = 32)
    private String channel;
    @Column(nullable = false, length = 32)
    private String status = "PENDING";
    @Column(name = "local_count", nullable = false)
    private int localCount;
    @Column(name = "channel_count", nullable = false)
    private int channelCount;
    @Column(name = "diff_count", nullable = false)
    private int diffCount;
    @Column(length = 512)
    private String note;
    @Column(name = "started_at")
    private Instant startedAt;
    @Column(name = "finished_at")
    private Instant finishedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public LocalDate getBizDate() { return bizDate; }
    public void setBizDate(LocalDate bizDate) { this.bizDate = bizDate; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setLocalCount(int localCount) { this.localCount = localCount; }
    public void setChannelCount(int channelCount) { this.channelCount = channelCount; }
    public void setDiffCount(int diffCount) { this.diffCount = diffCount; }
    public void setNote(String note) { this.note = note; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
}
