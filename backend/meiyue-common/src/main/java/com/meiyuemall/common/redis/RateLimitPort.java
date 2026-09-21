package com.meiyuemall.common.redis;

/**
 * 限流端口：优先 Redis；失败/关闭时内存窗口。
 */
public interface RateLimitPort {

    /**
     * @param bucket 业务桶，如 login / notify / api
     * @param key    客户端标识（通常 IP）
     * @param limit  窗口内上限
     * @param windowSeconds 窗口秒数
     * @return true 允许通过
     */
    boolean tryAcquire(String bucket, String key, int limit, int windowSeconds);
}
