package com.meiyuemall.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建正向运单。
 *
 * @param orderId         订单 ID
 * @param orderItemId     可选订单行（拆包按行）
 * @param carrierCode     承运商
 * @param trackingNo      运单号；开启 printEwaybill 时可空，由 MOCK 面单生成
 * @param packageSeq      包裹序号；空则自动递增
 * @param printEwaybill   是否 MOCK 打单（默认 true）
 * @param receiverName    收件人
 * @param receiverPhone   电话
 * @param receiverAddress 地址
 */
public record CreateForwardShipmentRequest(
        @NotNull Long orderId,
        Long orderItemId,
        @NotBlank String carrierCode,
        String trackingNo,
        Integer packageSeq,
        Boolean printEwaybill,
        String receiverName,
        String receiverPhone,
        String receiverAddress
) {}
