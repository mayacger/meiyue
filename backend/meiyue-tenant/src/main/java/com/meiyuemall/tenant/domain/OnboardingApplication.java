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
 * 入驻申请实体，映射 {@code onboarding_applications}。
 * <p>
 * 字段：applicantUserId 申请人；shopName/shopSlug 意向店名与 URL slug；
 * contactName/contactPhone 联系人；status 审核状态；tenantId 通过后回填。
 * </p>
 */
@Entity
@Table(name = "onboarding_applications")
public class OnboardingApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicant_user_id", nullable = false)
    private Long applicantUserId;

    @Column(name = "shop_name", nullable = false, length = 128)
    private String shopName;

    @Column(name = "shop_slug", nullable = false, unique = true, length = 64)
    private String shopSlug;

    @Column(name = "contact_name", nullable = false, length = 64)
    private String contactName;

    @Column(name = "contact_phone", nullable = false, length = 32)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OnboardingStatus status = OnboardingStatus.PENDING;

    @Column(name = "review_note", length = 512)
    private String reviewNote;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "tenant_id")
    private Long tenantId;

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

    public Long getApplicantUserId() {
        return applicantUserId;
    }

    public void setApplicantUserId(Long applicantUserId) {
        this.applicantUserId = applicantUserId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopSlug() {
        return shopSlug;
    }

    public void setShopSlug(String shopSlug) {
        this.shopSlug = shopSlug;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public OnboardingStatus getStatus() {
        return status;
    }

    public void setStatus(OnboardingStatus status) {
        this.status = status;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
