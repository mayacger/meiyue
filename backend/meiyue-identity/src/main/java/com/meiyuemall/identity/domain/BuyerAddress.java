package com.meiyuemall.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 买家收货地址实体，映射 {@code buyer_addresses}。
 * <p>
 * 字段：userId / receiverName / receiverPhone / province / city / district /
 * detailAddress / defaultAddress / createdAt / updatedAt
 * </p>
 */
@Entity
@Table(name = "buyer_addresses")
public class BuyerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "receiver_name", nullable = false, length = 64)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 32)
    private String receiverPhone;

    @Column(nullable = false, length = 64)
    private String province;

    @Column(nullable = false, length = 64)
    private String city;

    @Column(nullable = false, length = 64)
    private String district;

    @Column(name = "detail_address", nullable = false, length = 256)
    private String detailAddress;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

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
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
