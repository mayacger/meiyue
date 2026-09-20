package com.meiyuemall.boot.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meiyuemall.aiassist.service.AiAssistService;
import com.meiyuemall.common.redis.RedisAiTaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AI 异步队列消费者：IMAGE 记日志；VIDEO 调用 MOCK 生成并落库。
 */
@Component
public class AiTaskQueueJob {

    private static final Logger log = LoggerFactory.getLogger(AiTaskQueueJob.class);

    private final RedisAiTaskQueue queue;
    private final AiAssistService aiAssistService;
    private final ObjectMapper objectMapper;

    public AiTaskQueueJob(
            RedisAiTaskQueue queue,
            AiAssistService aiAssistService,
            ObjectMapper objectMapper
    ) {
        this.queue = queue;
        this.aiAssistService = aiAssistService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${meiyue.jobs.ai-queue-delay-ms:10000}")
    public void drain() {
        for (int i = 0; i < 20; i++) {
            String payload = queue.dequeue();
            if (payload == null) {
                return;
            }
            try {
                JsonNode node = objectMapper.readTree(payload);
                String type = node.path("type").asText("");
                if ("VIDEO".equalsIgnoreCase(type)) {
                    long taskId = node.path("taskId").asLong();
                    boolean ok = aiAssistService.processVideoTask(taskId);
                    log.info("AI 推广视频任务消费 taskId={} processed={}", taskId, ok);
                } else {
                    log.info("AI 异步任务消费 payload={}", payload);
                }
            } catch (Exception ex) {
                log.warn("AI 任务消费失败 payload={} err={}", payload, ex.getMessage());
            }
        }
    }
}
