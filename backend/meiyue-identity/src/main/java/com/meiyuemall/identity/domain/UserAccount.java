package com.meiyuemall.identity.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * 平台级用户账号实体，映射表 {@code user_accounts}。
 * <p>
 * 字段：
 * <ul>
 *   <li>{@code id} — 主键</li>
 *   <li>{@code username} — 登录名，唯一</li>
 *   <li>{@code passwordHash} — BCrypt 密码哈希</li>
 *   <li>{@code displayName} — 展示名</li>
 *   <li>{@code phone} — 手机号，可空</li>
 *   <li>{@code status} — ENABLED/DISABLED</li>
 *   <li>{@code roles} — 角色集合（user_roles）</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserStatus status = UserStatus.ENABLED;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_code", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private Set<RoleCode> roles = new HashSet<>();

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

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Set<RoleCode> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleCode> roles) {
        this.roles = roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
