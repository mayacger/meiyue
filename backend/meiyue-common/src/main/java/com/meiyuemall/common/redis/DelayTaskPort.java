package com.meiyuemall.common.redis;

import java.time.Instant;
import java.util.List;

/**
 * 延迟任务端口：Redis ZSET 实现；无 Redis 时用空实现，依赖 DB 扫描兜底。
 */
public interface DelayTaskPort {

    /** 关单：payload = orderId */
    String TYPE_ORDER_EXPIRE = "ORDER_EXPIRE";
    /** 售后 48h：payload = aftersaleId */
    String TYPE_AFTERSALE_AUTO = "AFTERSALE_AUTO";

    void schedule(String type, String payload, Instant runAt);

    /**
     * 取出已到期任务（并从队列移除，至少一次语义；业务需幂等）。
     */
    List<String> pollDue(String type, int limit);
}
