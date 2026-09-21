package com.meiyuemall.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 提交入驻申请。
 *
 * @param shopName     店铺名称
 * @param shopSlug     URL slug（小写字母数字短横线）
 * @param contactName  联系人
 * @param contactPhone 联系电话
 */
public record OnboardingApplyRequest(
        @NotBlank @Size(max = 128) String shopName,
        @NotBlank @Size(min = 2, max = 64)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug 仅允许小写字母、数字与短横线")
        String shopSlug,
        @NotBlank @Size(max = 64) String contactName,
        @NotBlank @Size(max = 32) String contactPhone
) {
}
