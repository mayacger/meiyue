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
 * 支付宝通道骨架（I4）。
 * <p>真实 SDK 验签/下单未接入；密钥仅配置注入，不入库。</p>
 */
@Component
public class AlipayPaymentChannelClient implements PaymentChannelClient {

    private static final Logger log = LoggerFactory.getLogger(AlipayPaymentChannelClient.class);

    private final PaymentProperties properties;

    public AlipayPaymentChannelClient(PaymentProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaymentChannel channel() {
        return PaymentChannel.ALIPAY;
    }

    @Override
    public String createPrepay(Payment payment) {
        ensureConfigured();
        log.info("ALIPAY createPrepay 占位 paymentNo={}", payment.getPaymentNo());
        throw new BusinessException(ErrorCode.BAD_REQUEST, "支付宝尚未完成真实对接，请使用 MOCK 或等待 I4 SDK 接入");
    }

    @Override
    public ChannelNotifyResult parseAndVerifyNotify(String rawBody, Map<String, String> headers) {
        ensureConfigured();
        log.warn("ALIPAY 回调验签占位，拒绝处理真实流量");
        return new ChannelNotifyResult(false, null, null, null, false, "ALIPAY_VERIFY_NOT_IMPLEMENTED");
    }

    @Override
    public ChannelQueryResult queryOrder(Payment payment) {
        ensureConfigured();
        log.info("ALIPAY queryOrder 占位 paymentNo={}", payment.getPaymentNo());
        return new ChannelQueryResult(false, false, null, null, "ALIPAY_QUERY_NOT_IMPLEMENTED");
    }

    private void ensureConfigured() {
        PaymentProperties.Alipay a = properties.alipay();
        if (a == null || isBlank(a.appId()) || isBlank(a.privateKey()) || isBlank(a.alipayPublicKey())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未配置支付宝商户参数（meiyue.payment.alipay.* / 环境变量）");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
