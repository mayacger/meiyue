package com.meiyuemall.decoration.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 店铺装修页。买家只读 PUBLISHED；商家编辑 DRAFT 后发布。
 * 楼层类型仅允许：BANNER / PRODUCT_RECOMMEND / IMAGE_TEXT / PRODUCT_GROUP（无直播）。
 */
@Entity
@Table(name = "store_pages")
public class StorePage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    @Column(name = "template_code", nullable = false, length = 64)
    private String templateCode;
    @Column(name = "theme_color", nullable = false, length = 32)
    private String themeColor = "#1a5f4a";
    @Column(name = "floors_json", nullable = false, columnDefinition = "TEXT")
    private String floorsJson;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StorePageStatus status = StorePageStatus.DRAFT;
    @Column(name = "published_at")
    private Instant publishedAt;
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
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getThemeColor() { return themeColor; }
    public void setThemeColor(String themeColor) { this.themeColor = themeColor; }
    public String getFloorsJson() { return floorsJson; }
    public void setFloorsJson(String floorsJson) { this.floorsJson = floorsJson; }
    public StorePageStatus getStatus() { return status; }
    public void setStatus(StorePageStatus status) { this.status = status; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
