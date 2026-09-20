package com.meiyuemall.payment;

/**
 * 支付网关占位实现：仅返回说明，不发起真实支付。
 */
public class PlaceholderPaymentGateway implements PaymentGatewayPort {

    @Override
    public String createPrepay(PaymentChannel channel, Long orderId, long amountCents, String idempotentKey) {
        // 字段说明：channel=通道；orderId=订单；amountCents=分；idempotentKey=幂等键
        return "PAYMENT_PLACEHOLDER: channel=" + channel
                + ", orderId=" + orderId
                + ", amountCents=" + amountCents
                + ", key=" + idempotentKey
                + "（本轮未对接微信/支付宝）";
    }
}
