package com.meiyuemall.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 买家发票抬头（I33 占位，无真实开票对接）。
 * <p>字段：userId / title 抬头 / taxNo 税号 / invoiceType PERSONAL|COMPANY / defaultAddress 是否默认</p>
 */
@Entity
@Table(name = "buyer_invoice_profiles")
public class BuyerInvoiceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(name = "tax_no", length = 64)
    private String taxNo;

    @Column(name = "invoice_type", nullable = false, length = 16)
    private String invoiceType = "PERSONAL";

    @Column(name = "is_default", nullable = false)
    private boolean defaultProfile;

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

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTaxNo() { return taxNo; }
    public void setTaxNo(String taxNo) { this.taxNo = taxNo; }
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    public boolean isDefaultProfile() { return defaultProfile; }
    public void setDefaultProfile(boolean defaultProfile) { this.defaultProfile = defaultProfile; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
