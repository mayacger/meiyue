package com.meiyuemall.support.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.support.dto.CreateNotificationRequest;
import com.meiyuemall.support.dto.NotificationResponse;
import com.meiyuemall.support.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 站内通知 API（I10 骨架）。
 * <ul>
 *   <li>GET /api/v1/notifications — 我的列表</li>
 *   <li>GET /api/v1/notifications/unread-count — 未读数</li>
 *   <li>POST /api/v1/notifications/{id}/read — 标已读</li>
 *   <li>POST /api/v1/notifications/read-all — 全部已读</li>
 *   <li>POST /api/v1/admin/notifications — 平台写入（联调）</li>
 * </ul>
 */
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/notifications")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<NotificationResponse>> listMine() {
        return ApiResponse.ok(notificationService.listMine());
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/notifications/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.ok(notificationService.unreadCount());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/notifications/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<NotificationResponse> markRead(@PathVariable Long id) {
        return ApiResponse.ok(notificationService.markRead(id));
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/notifications/read-all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Integer>> markAllRead() {
        return ApiResponse.ok(notificationService.markAllRead());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/admin/notifications")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<NotificationResponse> create(@Valid @RequestBody CreateNotificationRequest request) {
        return ApiResponse.ok(notificationService.create(request));
    }
}
