package com.meiyuemall.common.notify;

/**
 * 站内通知发布端口（I11）。
 * <p>由 meiyue-support 实现；业务模块仅依赖本接口，避免耦合通知表。</p>
 * <p>配置 {@code meiyue.notify.events-enabled=false} 时可整机关闭自动投递。</p>
 */
public interface NotificationPublisher {

    /**
     * 写入一条站内通知。
     *
     * @param userId   接收人用户 ID；null 则忽略
     * @param audience BUYER / SELLER / PLATFORM
     * @param title    标题
     * @param body     正文
     * @param category ORDER / AFTERSALE / COUPON / SYSTEM / REVIEW / SHIPMENT
     * @param refType  引用类型，如 ORDER / AFTERSALE / SHIPMENT
     * @param refId    引用业务 ID
     */
    void publish(
            Long userId,
            String audience,
            String title,
            String body,
            String category,
            String refType,
            String refId
    );
}
