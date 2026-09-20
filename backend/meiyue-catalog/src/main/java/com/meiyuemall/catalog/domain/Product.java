package com.meiyuemall.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品 SPU。字段 tenantId 强制隔离；status DRAFT/ON_SALE/OFF_SALE。
 */
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "category_id")
    private Long categoryId;
    @Column(nullable = false, length = 256)
    private String title;
    @Column(length = 512)
    private String subtitle;
    @Column(name = "detail_html", columnDefinition = "TEXT")
    private String detailHtml;
    /** I8：封面图 URL（人工或 AI 审核通过后） */
    @Column(name = "cover_image_url", length = 1024)
    private String coverImageUrl;
    /** I8：关联素材主键 */
    @Column(name = "cover_asset_id")
    private Long coverAssetId;
    /** I11：推广视频 URL（非直播） */
    @Column(name = "promo_video_url", length = 1024)
    private String promoVideoUrl;
    /** I11：推广视频素材 ID */
    @Column(name = "promo_video_asset_id")
    private Long promoVideoAssetId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductStatus status = ProductStatus.DRAFT;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductSku> skus = new ArrayList<>();

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
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getDetailHtml() { return detailHtml; }
    public void setDetailHtml(String detailHtml) { this.detailHtml = detailHtml; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public Long getCoverAssetId() { return coverAssetId; }
    public void setCoverAssetId(Long coverAssetId) { this.coverAssetId = coverAssetId; }
    public String getPromoVideoUrl() { return promoVideoUrl; }
    public void setPromoVideoUrl(String promoVideoUrl) { this.promoVideoUrl = promoVideoUrl; }
    public Long getPromoVideoAssetId() { return promoVideoAssetId; }
    public void setPromoVideoAssetId(Long promoVideoAssetId) { this.promoVideoAssetId = promoVideoAssetId; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<ProductSku> getSkus() { return skus; }
}
