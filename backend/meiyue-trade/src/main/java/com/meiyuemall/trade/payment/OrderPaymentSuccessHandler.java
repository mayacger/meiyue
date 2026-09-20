package com.meiyuemall.trade.payment;

import com.meiyuemall.payment.service.PaymentSuccessHandler;
import com.meiyuemall.payment.service.SettlementLedgerService;
import com.meiyuemall.trade.domain.Order;
import com.meiyuemall.trade.domain.OrderItem;
import com.meiyuemall.trade.domain.OrderStatus;
import com.meiyuemall.trade.repo.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付成功：标记订单 PAID + 写入周期结算账本（按店拆分）。
 */
@Component
public class OrderPaymentSuccessHandler implements PaymentSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderPaymentSuccessHandler.class);

    private final OrderRepository orderRepository;
    private final SettlementLedgerService settlementLedgerService;

    public OrderPaymentSuccessHandler(
            OrderRepository orderRepository,
            SettlementLedgerService settlementLedgerService
    ) {
        this.orderRepository = orderRepository;
        this.settlementLedgerService = settlementLedgerService;
    }

    @Override
    @Transactional
    public void onPaymentSuccess(Long orderId, String paymentNo, String channelTradeNo) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("支付成功但订单不存在 orderId={} paymentNo={}", orderId, paymentNo);
            return;
        }
        if (order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(Instant.now());
        }
        List<SettlementLedgerService.SaleLine> lines = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            lines.add(new SettlementLedgerService.SaleLine(
                    item.getTenantId(),
                    item.getId(),
                    item.getLineTotalCents(),
                    "订单支付入账 " + paymentNo
            ));
        }
        settlementLedgerService.recordOrderSalesIfAbsent(orderId, lines);
        log.info("订单已支付 orderId={} paymentNo={} tradeNo={}", orderId, paymentNo, channelTradeNo);
    }
}
