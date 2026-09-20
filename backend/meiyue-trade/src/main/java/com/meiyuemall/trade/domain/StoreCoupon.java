package com.meiyuemall.trade.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** 店券模板 */
@Entity
@Table(name = "store_coupons")
public class StoreCoupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(nullable = false, length = 32)
    private String code;
    @Column(nullable = false, length = 128)
    private String title;
    @Column(name = "discount_cents", nullable = false)
    private long discountCents;
    @Column(name = "min_spend_cents", nullable = false)
    private long minSpendCents;
    @Column(name = "total_quota", nullable = false)
    private int totalQuota;
    @Column(name = "claimed_count", nullable = false)
    private int claimedCount;
    @Column(nullable = false, length = 16)
    private String status = "ACTIVE";
    @Column(name = "starts_at", nullable = false)
    private Instant startsAt = Instant.now();
    @Column(name = "ends_at")
    private Instant endsAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getDiscountCents() { return discountCents; }
    public void setDiscountCents(long discountCents) { this.discountCents = discountCents; }
    public long getMinSpendCents() { return minSpendCents; }
    public void setMinSpendCents(long minSpendCents) { this.minSpendCents = minSpendCents; }
    public int getTotalQuota() { return totalQuota; }
    public void setTotalQuota(int totalQuota) { this.totalQuota = totalQuota; }
    public int getClaimedCount() { return claimedCount; }
    public void setClaimedCount(int claimedCount) { this.claimedCount = claimedCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getStartsAt() { return startsAt; }
    public void setStartsAt(Instant startsAt) { this.startsAt = startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public void setEndsAt(Instant endsAt) { this.endsAt = endsAt; }
    public Instant getCreatedAt() { return createdAt; }
}
