package com.meiyuemall.trade.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "buyer_user_id", nullable = false)
    private Long buyerUserId;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "sku_id", nullable = false)
    private Long skuId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now; updatedAt = now;
    }
    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getBuyerUserId() { return buyerUserId; }
    public void setBuyerUserId(Long buyerUserId) { this.buyerUserId = buyerUserId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
