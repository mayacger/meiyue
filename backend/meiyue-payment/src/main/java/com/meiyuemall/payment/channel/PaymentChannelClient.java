package com.meiyuemall.payment.channel;

import com.meiyuemall.payment.domain.Payment;
import com.meiyuemall.payment.domain.PaymentChannel;

/**
 * 支付通道端口（I4 骨架）。
 * <p>实现类：MOCK / 微信占位 / 支付宝占位；真实 SDK 对接在实现内完成，密钥只读配置。</p>
 */
public interface PaymentChannelClient {

    PaymentChannel channel();

    /**
     * 向通道创建预支付（返回给前端的调起参数摘要）。
     */
    String createPrepay(Payment payment);

    /**
     * 验签并解析异步通知。
     */
    ChannelNotifyResult parseAndVerifyNotify(String rawBody, java.util.Map<String, String> headers);

    /**
     * 主动查单（回调丢失时的补偿）。
     */
    ChannelQueryResult queryOrder(Payment payment);

    /**
     * I12：通道退款占位。
     * <p>MOCK 立即成功；WECHAT/ALIPAY 保留接口，未对接前返回失败或抛错。</p>
     *
     * @param payment         原支付单（须已 SUCCESS）
     * @param refundRequestNo 商户退款请求号（幂等，建议 aftersaleNo / aftersaleId）
     * @param amountCents     退款金额（分）
     */
    ChannelRefundResult refund(Payment payment, String refundRequestNo, long amountCents);
}
