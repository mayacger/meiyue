package com.meiyuemall.logistics.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "shipments")
public class Shipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "order_item_id")
    private Long orderItemId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ShipmentDirection direction;
    @Column(name = "carrier_code", length = 32)
    private String carrierCode;
    @Column(name = "tracking_no", length = 64)
    private String trackingNo;
    @Column(nullable = false, length = 32)
    private String status;
    @Column(name = "receiver_name", length = 64)
    private String receiverName;
    @Column(name = "receiver_phone", length = 32)
    private String receiverPhone;
    @Column(name = "receiver_address", length = 512)
    private String receiverAddress;
    @Column(name = "aftersale_id")
    private Long aftersaleId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist void onCreate() { Instant n=Instant.now(); createdAt=n; updatedAt=n; }
    @PreUpdate void onUpdate() { updatedAt=Instant.now(); }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public ShipmentDirection getDirection() { return direction; }
    public void setDirection(ShipmentDirection direction) { this.direction = direction; }
    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public Long getAftersaleId() { return aftersaleId; }
    public void setAftersaleId(Long aftersaleId) { this.aftersaleId = aftersaleId; }
    public Instant getUpdatedAt() { return updatedAt; }
}
