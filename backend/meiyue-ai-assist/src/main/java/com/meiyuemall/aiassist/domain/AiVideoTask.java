package com.meiyuemall.aiassist.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * AI 推广视频异步任务。
 * <p>字段：tenantId、productId（可选挂接）、status、prompt、resultAssetId、failReason。</p>
 * <p>非直播；默认由 MOCK Provider 异步产出占位 URL。</p>
 */
@Entity
@Table(name = "ai_video_tasks")
public class AiVideoTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "product_id")
    private Long productId;

    /** PENDING / RUNNING / SUCCEEDED / FAILED */
    @Column(nullable = false, length = 16)
    private String status = "PENDING";

    @Column(nullable = false, length = 1024)
    private String prompt;

    @Column(name = "result_asset_id")
    private Long resultAssetId;

    @Column(name = "fail_reason", length = 512)
    private String failReason;

    @Column(name = "created_by")
    private Long createdBy;

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
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public Long getResultAssetId() { return resultAssetId; }
    public void setResultAssetId(Long resultAssetId) { this.resultAssetId = resultAssetId; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
