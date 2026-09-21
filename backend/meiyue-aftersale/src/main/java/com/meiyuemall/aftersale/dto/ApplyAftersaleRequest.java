package com.meiyuemall.aftersale.dto;

import com.meiyuemall.aftersale.domain.AftersaleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 申请售后（I6 + I29 凭证图）。
 *
 * @param orderId            订单 ID
 * @param orderItemId        订单行（可空，单行订单时省略）
 * @param type               REFUND_ONLY / RETURN_REFUND
 * @param reason             原因
 * @param refundCents        退款金额（分）
 * @param evidenceImageUrls  凭证图 URL 列表（可选，最多 6 张）
 */
public record ApplyAftersaleRequest(
        @NotNull Long orderId,
        Long orderItemId,
        @NotNull AftersaleType type,
        @NotBlank String reason,
        @Min(1) long refundCents,
        List<@Size(max = 512) String> evidenceImageUrls
) {}
