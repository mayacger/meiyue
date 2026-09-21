package com.meiyuemall.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 平台运营配置项。
 * <p>
 * 字段说明：
 * <ul>
 *   <li>configKey — 主键键名，如 platform_fee_rate_bps</li>
 *   <li>configValue — 字符串值（费率基点 / 周期枚举 / 金额分）</li>
 *   <li>description — 人类可读说明</li>
 *   <li>updatedAt — 最后更新时间</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "platform_configs")
public class PlatformConfig {

    @Id
    @Column(name = "config_key", length = 64)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 256)
    private String configValue;

    @Column(length = 256)
    private String description;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
