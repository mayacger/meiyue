package com.meiyuemall.boot.job;

import com.meiyuemall.common.redis.RedisAiTaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 可选 AI 异步队列消费者：出队后记录日志（同步 API 仍为主路径）。
 */
@Component
public class AiTaskQueueJob {

    private static final Logger log = LoggerFactory.getLogger(AiTaskQueueJob.class);

    private final RedisAiTaskQueue queue;

    public AiTaskQueueJob(RedisAiTaskQueue queue) {
        this.queue = queue;
    }

    @Scheduled(fixedDelayString = "${meiyue.jobs.ai-queue-delay-ms:10000}")
    public void drain() {
        for (int i = 0; i < 20; i++) {
            String payload = queue.dequeue();
            if (payload == null) {
                return;
            }
            log.info("AI 异步任务消费（骨架） payload={}", payload);
        }
    }
}
