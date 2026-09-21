package com.meiyuemall.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 周期结算账本行（MVP 平台代收记账）。
 * 字段：tenantId 商家；entryType SALE/REFUND/ADJUST；amountCents 卖家应得；
 * periodKey 账期；status PENDING/SETTLED/VOID。
 */
@Entity
@Table(name = "settlement_ledgers")
public class SettlementLedger {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "order_item_id")
    private Long orderItemId;
    @Column(name = "entry_type", nullable = false, length = 32)
    private String entryType;
    @Column(name = "amount_cents", nullable = false)
    private long amountCents;
    @Column(nullable = false, length = 32)
    private String status = "PENDING";
    @Column(name = "period_key", nullable = false, length = 32)
    private String periodKey;
    @Column(length = 256)
    private String remark;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "settled_at")
    private Instant settledAt;

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public Long getOrderItemId() { return orderItemId; }
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPeriodKey() { return periodKey; }
    public void setPeriodKey(String periodKey) { this.periodKey = periodKey; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSettledAt() { return settledAt; }
    public void setSettledAt(Instant settledAt) { this.settledAt = settledAt; }
}
