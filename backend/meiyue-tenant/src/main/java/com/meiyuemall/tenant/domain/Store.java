package com.meiyuemall.tenant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 店铺实体。MVP：{@code tenant_id} 唯一，与租户 1:1。
 * 字段：tenantId、name、slug、status。
 */
@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, unique = true, length = 64)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StoreStatus status = StoreStatus.OPEN;

    /** 店铺简介 */
    @Column(length = 512)
    private String description;

    /** Logo URL（占位录入，非强制 OSS） */
    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    /** I31：默认运费（分） */
    @Column(name = "freight_cents", nullable = false)
    private long freightCents = 0;

    /** I31：包邮门槛（分）；null 表示无包邮 */
    @Column(name = "free_shipping_threshold_cents")
    private Long freeShippingThresholdCents;

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
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public StoreStatus getStatus() {
        return status;
    }

    public void setStatus(StoreStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public long getFreightCents() {
        return freightCents;
    }

    public void setFreightCents(long freightCents) {
        this.freightCents = freightCents;
    }

    public Long getFreeShippingThresholdCents() {
        return freeShippingThresholdCents;
    }

    public void setFreeShippingThresholdCents(Long freeShippingThresholdCents) {
        this.freeShippingThresholdCents = freeShippingThresholdCents;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
