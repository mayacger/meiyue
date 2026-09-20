package com.meiyuemall.payment;

/**
 * 支付网关端口占位（无实现）。
 * <p>
 * I3 可提供模拟实现（始终成功）；I4 再接微信/支付宝官方 SDK，
 * 并补齐：回调验签、幂等、主动查单、日对账、原路退款。
 * </p>
 */
public interface PaymentGatewayPort {

    /**
     * 向通道创建预支付单（占位方法签名）。
     *
     * @param channel       支付通道
     * @param orderId       平台订单 ID
     * @param amountCents   金额（分），以后端订单为准
     * @param idempotentKey 幂等键（通常为支付单号）
     * @return 通道侧预支付凭证摘要（脚手架恒返回说明文字）
     */
    String createPrepay(PaymentChannel channel, Long orderId, long amountCents, String idempotentKey);
}
