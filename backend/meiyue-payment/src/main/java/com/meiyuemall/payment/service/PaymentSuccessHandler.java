package com.meiyuemall.payment.service;

/**
 * 支付成功领域回调（由 trade 模块实现：标记订单已付 + 记账）。
 */
public interface PaymentSuccessHandler {
    void onPaymentSuccess(Long orderId, String paymentNo, String channelTradeNo);
}
