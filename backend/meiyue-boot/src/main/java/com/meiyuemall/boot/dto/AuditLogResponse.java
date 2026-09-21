package com.meiyuemall.boot.dto;

/**
 * 审计日志只读视图（I24）。
 * <p>不含密钥/支付凭证明文；detail 仅业务摘要。</p>
 *
 * @param id           主键
 * @param actorUserId  操作者用户 ID
 * @param actorType    BUYER / SELLER / PLATFORM / SYSTEM
 * @param tenantId     租户（可空）
 * @param action       动作码，如 PRODUCT_UPDATE
 * @param resourceType 资源类型
 * @param resourceId   资源 ID
 * @param outcome      SUCCESS / FAILURE
 * @param detail       摘要
 * @param traceId      链路追踪
 * @param ip           客户端 IP
 * @param createdAt    时间 ISO
 */
public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String actorType,
        Long tenantId,
        String action,
        String resourceType,
        String resourceId,
        String outcome,
        String detail,
        String traceId,
        String ip,
        String createdAt
) {}
