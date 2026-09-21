package com.meiyuemall.support.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.support.domain.Notification;
import com.meiyuemall.support.dto.CreateNotificationRequest;
import com.meiyuemall.support.dto.NotificationResponse;
import com.meiyuemall.support.repo.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 站内通知骨架：写入、列表、标已读、未读数。
 * <p>不接推送 / 邮件 / IM；供买家端与商家端轮询。</p>
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * 写入一条通知（平台管理端或后续业务事件可调用）。
     */
    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification n = new Notification();
        n.setUserId(request.userId());
        n.setAudience(request.audience().trim().toUpperCase());
        n.setTitle(request.title().trim());
        n.setBody(request.body().trim());
        n.setCategory(request.category() == null || request.category().isBlank()
                ? "SYSTEM" : request.category().trim().toUpperCase());
        n.setRefType(blankToNull(request.refType()));
        n.setRefId(blankToNull(request.refId()));
        notificationRepository.save(n);
        return toResponse(n);
    }

    /** 当前登录用户的通知列表（新→旧） */
    @Transactional(readOnly = true)
    public List<NotificationResponse> listMine() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    /** 未读数量 */
    @Transactional(readOnly = true)
    public Map<String, Long> unreadCount() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        return Map.of("unread", notificationRepository.countByUserIdAndReadAtIsNull(userId));
    }

    /** 标记单条已读（仅本人） */
    @Transactional
    public NotificationResponse markRead(Long id) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        Notification n = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "通知不存在"));
        if (n.getReadAt() == null) {
            n.setReadAt(Instant.now());
        }
        return toResponse(n);
    }

    /** 全部标已读 */
    @Transactional
    public Map<String, Integer> markAllRead() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        Instant now = Instant.now();
        int n = 0;
        for (Notification item : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            if (item.getReadAt() == null) {
                item.setReadAt(now);
                n++;
            }
        }
        return Map.of("marked", n);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getAudience(),
                n.getTitle(),
                n.getBody(),
                n.getCategory(),
                n.getRefType(),
                n.getRefId(),
                n.getReadAt() != null,
                n.getCreatedAt() == null ? null : n.getCreatedAt().toString()
        );
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
