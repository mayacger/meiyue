package com.meiyuemall.payment.channel;

import com.meiyuemall.payment.domain.Payment;
import com.meiyuemall.payment.domain.PaymentChannel;
import com.meiyuemall.payment.domain.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** I12：MOCK 通道退款占位单测 */
class MockPaymentChannelClientTest {

    private final MockPaymentChannelClient client = new MockPaymentChannelClient();

    @Test
    void refundReturnsSuccessWithMockNo() {
        Payment p = new Payment();
        p.setPaymentNo("P123");
        p.setChannel(PaymentChannel.MOCK);
        p.setStatus(PaymentStatus.SUCCESS);
        p.setAmountCents(1000);

        ChannelRefundResult r = client.refund(p, "AS99", 500);
        assertTrue(r.success());
        assertEquals("mock_rf_AS99", r.channelRefundNo());
    }

    @Test
    void refundIdempotentRequestNoInTradeNo() {
        Payment p = new Payment();
        p.setPaymentNo("P1");
        ChannelRefundResult a = client.refund(p, "AS1", 100);
        ChannelRefundResult b = client.refund(p, "AS1", 100);
        assertEquals(a.channelRefundNo(), b.channelRefundNo());
    }
}
