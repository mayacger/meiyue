package com.meiyuemall.identity.dto;

/**
 * 地址响应。
 *
 * @param id             主键
 * @param receiverName   收件人
 * @param receiverPhone  手机
 * @param province       省
 * @param city           市
 * @param district       区县
 * @param detailAddress  详细地址
 * @param defaultAddress 是否默认
 */
public record AddressResponse(
        Long id,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detailAddress,
        boolean defaultAddress
) {}
