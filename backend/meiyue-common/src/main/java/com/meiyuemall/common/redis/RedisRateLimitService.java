package com.meiyuemall.common.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis INCR 限流；异常时降级到进程内滑动窗口。
 */
@Component
public class RedisRateLimitService implements RateLimitPort {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitService.class);
    private static final String KEY_PREFIX = "meiyue:rl:";

    private final StringRedisTemplate redis;
    private final boolean redisEnabled;
    private final ConcurrentHashMap<String, Window> memory = new ConcurrentHashMap<>();

    public RedisRateLimitService(
            StringRedisTemplate redis,
            @Value("${meiyue.redis.rate-limit-enabled:true}") boolean redisEnabled
    ) {
        this.redis = redis;
        this.redisEnabled = redisEnabled;
    }

    @Override
    public boolean tryAcquire(String bucket, String key, int limit, int windowSeconds) {
        if (redisEnabled) {
            try {
                String redisKey = KEY_PREFIX + bucket + ":" + key;
                Long count = redis.opsForValue().increment(redisKey);
                if (count != null && count == 1L) {
                    redis.expire(redisKey, Duration.ofSeconds(windowSeconds));
                }
                return count == null || count <= limit;
            } catch (Exception ex) {
                log.debug("Redis 限流失败，降级内存: {}", ex.getMessage());
            }
        }
        return memoryAcquire(bucket + "|" + key, limit, windowSeconds);
    }

    private boolean memoryAcquire(String key, int limit, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;
        Window w = memory.compute(key, (k, old) -> {
            if (old == null || now - old.windowStartMs >= windowMs) {
                return new Window(now, new AtomicInteger(1));
            }
            old.count.incrementAndGet();
            return old;
        });
        return w.count.get() <= limit;
    }

    private record Window(long windowStartMs, AtomicInteger count) {}
}
