package com.meiyuemall.payment.channel;

import com.meiyuemall.payment.domain.Payment;
import com.meiyuemall.payment.domain.PaymentChannel;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MOCK 通道：本地联调；不发起真实网络请求。
 */
@Component
public class MockPaymentChannelClient implements PaymentChannelClient {

    @Override
    public PaymentChannel channel() {
        return PaymentChannel.MOCK;
    }

    @Override
    public String createPrepay(Payment payment) {
        return "MOCK_PREPAY:" + payment.getPaymentNo();
    }

    @Override
    public ChannelNotifyResult parseAndVerifyNotify(String rawBody, Map<String, String> headers) {
        // MOCK 通知约定：paymentNo=xxx&channelTradeNo=yyy&amountCents=zzz&success=true
        String paymentNo = extract(rawBody, "paymentNo");
        String tradeNo = extract(rawBody, "channelTradeNo");
        String amount = extract(rawBody, "amountCents");
        boolean success = !"false".equalsIgnoreCase(extract(rawBody, "success"));
        Long amountCents = amount == null || amount.isBlank() ? null : Long.parseLong(amount);
        return new ChannelNotifyResult(true, paymentNo, tradeNo, amountCents, success, preview(rawBody));
    }

    @Override
    public ChannelQueryResult queryOrder(Payment payment) {
        if (payment.getChannelTradeNo() != null && payment.getChannelTradeNo().startsWith("mock_")) {
            return new ChannelQueryResult(true, true, payment.getChannelTradeNo(), payment.getAmountCents(), "MOCK_QUERY_PAID");
        }
        // 未支付：返回未找到/未付，便于补偿任务跳过
        return new ChannelQueryResult(true, false, null, payment.getAmountCents(), "MOCK_QUERY_PENDING");
    }

    private static String extract(String raw, String key) {
        if (raw == null) return null;
        for (String part : raw.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return null;
    }

    private static String preview(String raw) {
        if (raw == null) return null;
        return raw.length() > 200 ? raw.substring(0, 200) : raw;
    }
}
