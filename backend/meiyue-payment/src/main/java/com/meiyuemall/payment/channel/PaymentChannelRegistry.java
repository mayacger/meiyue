package com.meiyuemall.payment.channel;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.payment.domain.PaymentChannel;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** 按通道枚举路由到具体 Client */
@Component
public class PaymentChannelRegistry {

    private final Map<PaymentChannel, PaymentChannelClient> clients = new EnumMap<>(PaymentChannel.class);

    public PaymentChannelRegistry(List<PaymentChannelClient> clientList) {
        for (PaymentChannelClient client : clientList) {
            clients.put(client.channel(), client);
        }
    }

    public PaymentChannelClient get(PaymentChannel channel) {
        PaymentChannelClient client = clients.get(channel);
        if (client == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的支付通道: " + channel);
        }
        return client;
    }
}
