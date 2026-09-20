package com.meiyuemall.trade.job;

import com.meiyuemall.trade.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付超时关单任务（I3 暂用固定调度；I4+ 可改为 Redis 延迟队列）。
 * 每 60 秒扫描 PENDING_PAYMENT 且已过 pay_expire_at 的订单。
 */
@Component
public class OrderExpireJob {

    private static final Logger log = LoggerFactory.getLogger(OrderExpireJob.class);

    private final OrderService orderService;

    public OrderExpireJob(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelayString = "60000")
    public void sweep() {
        int n = orderService.cancelExpiredOrders();
        if (n > 0) {
            log.info("支付超时关单 {} 笔", n);
        }
    }
}
