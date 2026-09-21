package com.meiyuemall.payment.channel;

/**
 * 通道退款结果。
 *
 * @param success         是否成功
 * @param channelRefundNo 通道退款流水号
 * @param message         说明 / 失败原因
 */
public record ChannelRefundResult(boolean success, String channelRefundNo, String message) {
    public static ChannelRefundResult ok(String channelRefundNo) {
        return new ChannelRefundResult(true, channelRefundNo, "OK");
    }

    public static ChannelRefundResult fail(String message) {
        return new ChannelRefundResult(false, null, message);
    }
}
