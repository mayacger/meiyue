package com.meiyuemall.common.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis ZSET 延迟队列：score=到期 epoch 毫秒，member=业务 payload。
 */
@Component
public class RedisDelayTaskService implements DelayTaskPort {

    private static final Logger log = LoggerFactory.getLogger(RedisDelayTaskService.class);
    private static final String KEY_PREFIX = "meiyue:delay:";

    private final StringRedisTemplate redis;
    private final boolean enabled;

    public RedisDelayTaskService(
            StringRedisTemplate redis,
            @org.springframework.beans.factory.annotation.Value("${meiyue.redis.delay-queue-enabled:true}") boolean enabled
    ) {
        this.redis = redis;
        this.enabled = enabled;
    }

    @Override
    public void schedule(String type, String payload, Instant runAt) {
        if (!enabled || payload == null || runAt == null) {
            return;
        }
        try {
            redis.opsForZSet().add(KEY_PREFIX + type, payload, runAt.toEpochMilli());
        } catch (Exception ex) {
            log.warn("延迟任务入队失败 type={} payload={}: {}", type, payload, ex.getMessage());
        }
    }

    @Override
    public List<String> pollDue(String type, int limit) {
        if (!enabled) {
            return List.of();
        }
        List<String> due = new ArrayList<>();
        try {
            String key = KEY_PREFIX + type;
            long now = Instant.now().toEpochMilli();
            Set<String> members = redis.opsForZSet().rangeByScore(key, 0, now, 0, Math.max(1, limit));
            if (members == null || members.isEmpty()) {
                return due;
            }
            for (String m : members) {
                Long removed = redis.opsForZSet().remove(key, m);
                if (removed != null && removed > 0) {
                    due.add(m);
                }
            }
        } catch (Exception ex) {
            log.warn("延迟任务出队失败 type={}: {}", type, ex.getMessage());
        }
        return due;
    }
}
