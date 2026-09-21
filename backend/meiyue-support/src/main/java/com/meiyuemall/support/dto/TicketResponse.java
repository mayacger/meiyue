package com.meiyuemall.support.dto;

/**
 * 工单响应。
 *
 * @param id          主键
 * @param ticketNo    工单号
 * @param userId      发起人
 * @param tenantId    商家租户，可空
 * @param subject     标题
 * @param body        正文
 * @param category    分类
 * @param status      OPEN / REPLIED / CLOSED
 * @param sellerReply 商家回复
 * @param adminReply  平台回复
 * @param repliedAt   回复时间
 * @param closedAt    关闭时间
 * @param createdAt   创建时间
 */
public record TicketResponse(
        Long id,
        String ticketNo,
        Long userId,
        Long tenantId,
        String subject,
        String body,
        String category,
        String status,
        String sellerReply,
        String adminReply,
        String repliedAt,
        String closedAt,
        String createdAt
) {}
