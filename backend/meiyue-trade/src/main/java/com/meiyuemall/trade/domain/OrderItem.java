package com.meiyuemall.trade.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "sku_id", nullable = false)
    private Long skuId;
    @Column(name = "product_title", nullable = false, length = 256)
    private String productTitle;
    @Column(name = "sku_code", nullable = false, length = 64)
    private String skuCode;
    @Column(name = "spec_text", length = 256)
    private String specText;
    @Column(name = "unit_price_cents", nullable = false)
    private long unitPriceCents;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "line_total_cents", nullable = false)
    private long lineTotalCents;

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getSpecText() { return specText; }
    public void setSpecText(String specText) { this.specText = specText; }
    public long getUnitPriceCents() { return unitPriceCents; }
    public void setUnitPriceCents(long unitPriceCents) { this.unitPriceCents = unitPriceCents; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public long getLineTotalCents() { return lineTotalCents; }
    public void setLineTotalCents(long lineTotalCents) { this.lineTotalCents = lineTotalCents; }
}
