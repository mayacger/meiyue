package com.meiyuemall.trade.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** 商品评价：订单行完成后一条 */
@Entity
@Table(name = "product_reviews")
public class ProductReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "buyer_user_id", nullable = false)
    private Long buyerUserId;
    @Column(nullable = false)
    private int rating;
    @Column(nullable = false, length = 1000)
    private String content;
    @Column(name = "seller_reply", length = 1000)
    private String sellerReply;
    @Column(name = "replied_at")
    private Instant repliedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getBuyerUserId() { return buyerUserId; }
    public void setBuyerUserId(Long buyerUserId) { this.buyerUserId = buyerUserId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSellerReply() { return sellerReply; }
    public void setSellerReply(String sellerReply) { this.sellerReply = sellerReply; }
    public Instant getRepliedAt() { return repliedAt; }
    public void setRepliedAt(Instant repliedAt) { this.repliedAt = repliedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
