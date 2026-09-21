package com.meiyuemall.tenant.dto;

import jakarta.validation.constraints.Size;

/**
 * 平台审核请求。
 *
 * @param reviewNote 审核备注（拒绝时建议填写）
 */
public record ReviewOnboardingRequest(
        @Size(max = 512) String reviewNote
) {
}
