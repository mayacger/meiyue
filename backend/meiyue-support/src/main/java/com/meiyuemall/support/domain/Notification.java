package com.meiyuemall.support.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 站内通知骨架（I10）。
 * <p>
 * 字段说明：
 * <ul>
 *   <li>userId — 接收人用户 ID</li>
 *   <li>audience — BUYER / SELLER / PLATFORM</li>
 *   <li>title / body — 标题与摘要正文</li>
 *   <li>category — ORDER / AFTERSALE / COUPON / SYSTEM / REVIEW</li>
 *   <li>refType / refId — 可选业务引用（如 ORDER、REVIEW）</li>
 *   <li>readAt — 已读时间；空=未读</li>
 * </ul>
 * 非推送通道；无 IM。
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 接收人用户主键 */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 受众：BUYER | SELLER | PLATFORM */
    @Column(nullable = false, length = 16)
    private String audience;

    /** 通知标题 */
    @Column(nullable = false, length = 128)
    private String title;

    /** 通知正文摘要 */
    @Column(nullable = false, length = 512)
    private String body;

    /** 业务分类 */
    @Column(nullable = false, length = 32)
    private String category = "SYSTEM";

    /** 引用类型，可空 */
    @Column(name = "ref_type", length = 32)
    private String refType;

    /** 引用业务 ID，可空 */
    @Column(name = "ref_id", length = 64)
    private String refId;

    /** 已读时间；null 表示未读 */
    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
    public Instant getCreatedAt() { return createdAt; }
}
