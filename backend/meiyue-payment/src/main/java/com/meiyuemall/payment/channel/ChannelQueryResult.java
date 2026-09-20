package com.meiyuemall.payment.channel;

/**
 * 主动查单结果。
 *
 * @param found           通道是否找到单据
 * @param paid            是否已支付
 * @param channelTradeNo  通道流水
 * @param amountCents     金额分
 * @param rawPreview      脱敏预览
 */
public record ChannelQueryResult(
        boolean found,
        boolean paid,
        String channelTradeNo,
        Long amountCents,
        String rawPreview
) {}
