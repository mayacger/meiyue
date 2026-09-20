package com.meiyuemall.support.notify;

import com.meiyuemall.common.notify.NotificationPublisher;
import com.meiyuemall.support.dto.CreateNotificationRequest;
import com.meiyuemall.support.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 站内通知发布实现：受 {@code meiyue.notify.events-enabled} 开关控制。
 */
@Component
public class NotificationPublisherImpl implements NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisherImpl.class);

    private final NotificationService notificationService;
    private final boolean eventsEnabled;

    public NotificationPublisherImpl(
            NotificationService notificationService,
            @Value("${meiyue.notify.events-enabled:true}") boolean eventsEnabled
    ) {
        this.notificationService = notificationService;
        this.eventsEnabled = eventsEnabled;
    }

    @Override
    public void publish(
            Long userId,
            String audience,
            String title,
            String body,
            String category,
            String refType,
            String refId
    ) {
        if (!eventsEnabled) {
            return;
        }
        if (userId == null) {
            return;
        }
        try {
            notificationService.create(new CreateNotificationRequest(
                    userId,
                    audience == null ? "BUYER" : audience,
                    title,
                    body,
                    category,
                    refType,
                    refId
            ));
        } catch (Exception ex) {
            // 通知失败不影响主交易链路
            log.warn("站内通知写入失败 userId={} title={} err={}", userId, title, ex.getMessage());
        }
    }
}
