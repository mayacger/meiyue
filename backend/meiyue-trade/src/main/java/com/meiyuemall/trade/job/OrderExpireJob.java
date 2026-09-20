package com.meiyuemall.trade.job;

import com.meiyuemall.common.redis.DelayTaskPort;
import com.meiyuemall.trade.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付超时关单：优先消费 Redis 延迟队列；{@code meiyue.jobs.db-fallback-enabled=true} 时再扫 DB。
 */
@Component
public class OrderExpireJob {

    private static final Logger log = LoggerFactory.getLogger(OrderExpireJob.class);

    private final OrderService orderService;
    private final DelayTaskPort delayTaskPort;
    private final boolean dbFallback;

    public OrderExpireJob(
            OrderService orderService,
            DelayTaskPort delayTaskPort,
            @Value("${meiyue.jobs.db-fallback-enabled:true}") boolean dbFallback
    ) {
        this.orderService = orderService;
        this.delayTaskPort = delayTaskPort;
        this.dbFallback = dbFallback;
    }

    @Scheduled(fixedDelayString = "${meiyue.jobs.order-expire-delay-ms:15000}")
    public void sweep() {
        int n = 0;
        for (String payload : delayTaskPort.pollDue(DelayTaskPort.TYPE_ORDER_EXPIRE, 100)) {
            try {
                if (orderService.cancelExpiredById(Long.parseLong(payload))) {
                    n++;
                }
            } catch (Exception ex) {
                log.warn("Redis 关单失败 orderId={}: {}", payload, ex.getMessage());
            }
        }
        if (dbFallback) {
            n += orderService.cancelExpiredOrders();
        }
        if (n > 0) {
            log.info("支付超时关单 {} 笔", n);
        }
    }
}
