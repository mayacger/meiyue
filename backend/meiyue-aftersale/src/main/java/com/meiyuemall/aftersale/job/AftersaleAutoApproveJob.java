package com.meiyuemall.aftersale.job;

import com.meiyuemall.aftersale.service.AftersaleService;
import com.meiyuemall.common.redis.DelayTaskPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 售后 48h 自动同意：优先 Redis 延迟队列，可选 DB 扫描兜底。
 */
@Component
public class AftersaleAutoApproveJob {

    private static final Logger log = LoggerFactory.getLogger(AftersaleAutoApproveJob.class);

    private final AftersaleService aftersaleService;
    private final DelayTaskPort delayTaskPort;
    private final boolean dbFallback;

    public AftersaleAutoApproveJob(
            AftersaleService aftersaleService,
            DelayTaskPort delayTaskPort,
            @Value("${meiyue.jobs.db-fallback-enabled:true}") boolean dbFallback
    ) {
        this.aftersaleService = aftersaleService;
        this.delayTaskPort = delayTaskPort;
        this.dbFallback = dbFallback;
    }

    @Scheduled(fixedDelayString = "${meiyue.jobs.aftersale-auto-delay-ms:20000}")
    public void sweep() {
        int n = 0;
        for (String payload : delayTaskPort.pollDue(DelayTaskPort.TYPE_AFTERSALE_AUTO, 100)) {
            try {
                if (aftersaleService.autoApproveById(Long.parseLong(payload))) {
                    n++;
                }
            } catch (Exception ex) {
                log.warn("Redis 售后自动同意失败 id={}: {}", payload, ex.getMessage());
            }
        }
        if (dbFallback) {
            n += aftersaleService.autoApproveExpired();
        }
        if (n > 0) {
            log.info("售后超时自动同意 {} 笔", n);
        }
    }
}
