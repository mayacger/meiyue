package com.meiyuemall.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 买家商品收藏（I22）。
 * 字段：userId / productId / createdAt
 */
@Entity
@Table(name = "product_favorites")
public class ProductFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Instant getCreatedAt() { return createdAt; }
}
