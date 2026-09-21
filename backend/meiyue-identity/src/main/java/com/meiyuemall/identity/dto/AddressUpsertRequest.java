package com.meiyuemall.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 地址创建/更新请求。
 *
 * @param receiverName   收件人
 * @param receiverPhone  手机
 * @param province       省
 * @param city           市
 * @param district       区县
 * @param detailAddress  详细地址
 * @param defaultAddress 是否默认
 */
public record AddressUpsertRequest(
        @NotBlank @Size(max = 64) String receiverName,
        @NotBlank @Size(max = 32) String receiverPhone,
        @NotBlank @Size(max = 64) String province,
        @NotBlank @Size(max = 64) String city,
        @NotBlank @Size(max = 64) String district,
        @NotBlank @Size(max = 256) String detailAddress,
        Boolean defaultAddress
) {}
