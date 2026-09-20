package com.meiyuemall.aftersale.job;

import com.meiyuemall.aftersale.service.AftersaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 售后 48h 未审核自动同意（规划 C13）。
 */
@Component
public class AftersaleAutoApproveJob {

    private static final Logger log = LoggerFactory.getLogger(AftersaleAutoApproveJob.class);

    private final AftersaleService aftersaleService;

    public AftersaleAutoApproveJob(AftersaleService aftersaleService) {
        this.aftersaleService = aftersaleService;
    }

    @Scheduled(fixedDelayString = "60000")
    public void sweep() {
        int n = aftersaleService.autoApproveExpired();
        if (n > 0) {
            log.info("售后超时自动同意 {} 笔", n);
        }
    }
}
