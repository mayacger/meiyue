package com.meiyuemall.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 买家浏览足迹（I30）。
 * <p>同一用户对同一商品只保留一行，browsedAt 刷新为最近浏览时间。</p>
 */
@Entity
@Table(name = "product_browse_histories")
public class ProductBrowseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "browsed_at", nullable = false)
    private Instant browsedAt;

    @PrePersist
    void onCreate() {
        if (browsedAt == null) {
            browsedAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Instant getBrowsedAt() { return browsedAt; }
    public void setBrowsedAt(Instant browsedAt) { this.browsedAt = browsedAt; }
}
