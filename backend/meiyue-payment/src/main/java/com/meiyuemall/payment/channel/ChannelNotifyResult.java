package com.meiyuemall.payment.channel;

/**
 * 通道回调解析结果。
 *
 * @param verified        验签是否通过
 * @param paymentNo       平台支付号（out_trade_no）
 * @param channelTradeNo  通道流水号
 * @param amountCents     金额（分），以后端订单为准校验
 * @param success         通道侧是否支付成功
 * @param rawPreview      脱敏预览
 */
public record ChannelNotifyResult(
        boolean verified,
        String paymentNo,
        String channelTradeNo,
        Long amountCents,
        boolean success,
        String rawPreview
) {}
