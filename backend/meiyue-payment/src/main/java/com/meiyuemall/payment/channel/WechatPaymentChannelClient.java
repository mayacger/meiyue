package com.meiyuemall.payment.channel;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.payment.config.PaymentProperties;
import com.meiyuemall.payment.domain.Payment;
import com.meiyuemall.payment.domain.PaymentChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信支付通道骨架（I4）。
 * <p>真实 SDK/APIv3 验签与下单未接入；配置齐全前调用将返回明确错误。
 * 密钥字段：{@link PaymentProperties.Wechat}，仅配置/环境变量注入。</p>
 */
@Component
public class WechatPaymentChannelClient implements PaymentChannelClient {

    private static final Logger log = LoggerFactory.getLogger(WechatPaymentChannelClient.class);

    private final PaymentProperties properties;

    public WechatPaymentChannelClient(PaymentProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaymentChannel channel() {
        return PaymentChannel.WECHAT;
    }

    @Override
    public String createPrepay(Payment payment) {
        ensureConfigured();
        // TODO I4+：调用微信统一下单 / JSAPI prepay，返回调起参数
        log.info("WECHAT createPrepay 占位 paymentNo={}", payment.getPaymentNo());
        throw new BusinessException(ErrorCode.BAD_REQUEST, "微信支付尚未完成真实对接，请使用 MOCK 或等待 I4 SDK 接入");
    }

    @Override
    public ChannelNotifyResult parseAndVerifyNotify(String rawBody, Map<String, String> headers) {
        ensureConfigured();
        // TODO：用平台证书/APIv3 密钥验签 Wechatpay-Signature
        log.warn("WECHAT 回调验签占位，拒绝处理真实流量");
        return new ChannelNotifyResult(false, null, null, null, false, "WECHAT_VERIFY_NOT_IMPLEMENTED");
    }

    @Override
    public ChannelQueryResult queryOrder(Payment payment) {
        ensureConfigured();
        // TODO：GET /v3/pay/transactions/out-trade-no/{out_trade_no}
        log.info("WECHAT queryOrder 占位 paymentNo={}", payment.getPaymentNo());
        return new ChannelQueryResult(false, false, null, null, "WECHAT_QUERY_NOT_IMPLEMENTED");
    }

    @Override
    public ChannelRefundResult refund(Payment payment, String refundRequestNo, long amountCents) {
        ensureConfigured();
        // TODO：POST /v3/refund/domestic/refunds
        log.info("WECHAT refund 占位 paymentNo={} refundRequestNo={} amount={}",
                payment.getPaymentNo(), refundRequestNo, amountCents);
        return ChannelRefundResult.fail("WECHAT_REFUND_NOT_IMPLEMENTED");
    }

    private void ensureConfigured() {
        PaymentProperties.Wechat w = properties.wechat();
        if (w == null || isBlank(w.mchId()) || isBlank(w.apiV3Key())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未配置微信支付商户参数（meiyue.payment.wechat.* / 环境变量）");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
