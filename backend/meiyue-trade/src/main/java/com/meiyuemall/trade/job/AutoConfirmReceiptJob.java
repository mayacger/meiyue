package com.meiyuemall.trade.job;

import com.meiyuemall.trade.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * I32：签收后到期自动确认收货。
 */
@Component
public class AutoConfirmReceiptJob {

    private static final Logger log = LoggerFactory.getLogger(AutoConfirmReceiptJob.class);

    private final OrderService orderService;

    public AutoConfirmReceiptJob(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelayString = "${meiyue.jobs.auto-confirm-delay-ms:20000}")
    public void sweep() {
        int n = orderService.autoConfirmDueOrders();
        if (n > 0) {
            log.info("自动确认收货 {} 笔", n);
        }
    }
}
