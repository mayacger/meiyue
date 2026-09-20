package com.meiyuemall.common.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 可选 AI 异步任务队列（Redis List）。入队后由定时任务消费；失败不影响同步 API。
 */
@Component
public class RedisAiTaskQueue {

    private static final Logger log = LoggerFactory.getLogger(RedisAiTaskQueue.class);
    public static final String KEY = "meiyue:ai:queue";

    private final StringRedisTemplate redis;
    private final boolean enabled;

    public RedisAiTaskQueue(
            StringRedisTemplate redis,
            @Value("${meiyue.redis.ai-queue-enabled:true}") boolean enabled
    ) {
        this.redis = redis;
        this.enabled = enabled;
    }

    public void enqueue(String payloadJson) {
        if (!enabled || payloadJson == null) {
            return;
        }
        try {
            redis.opsForList().rightPush(KEY, payloadJson);
        } catch (Exception ex) {
            log.warn("AI 任务入队失败: {}", ex.getMessage());
        }
    }

    public String dequeue() {
        if (!enabled) {
            return null;
        }
        try {
            return redis.opsForList().leftPop(KEY);
        } catch (Exception ex) {
            log.debug("AI 任务出队失败: {}", ex.getMessage());
            return null;
        }
    }
}
