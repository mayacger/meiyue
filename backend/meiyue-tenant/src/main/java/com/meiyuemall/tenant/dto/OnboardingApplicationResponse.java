package com.meiyuemall.tenant.dto;

import java.time.Instant;

/**
 * 入驻申请视图。
 */
public record OnboardingApplicationResponse(
        Long id,
        Long applicantUserId,
        String shopName,
        String shopSlug,
        String contactName,
        String contactPhone,
        String status,
        String reviewNote,
        Long tenantId,
        Instant createdAt,
        Instant reviewedAt
) {
}
