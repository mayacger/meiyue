package com.meiyuemall.identity.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.identity.domain.UserStatus;
import com.meiyuemall.identity.dto.AdminUserResponse;
import com.meiyuemall.identity.service.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 平台用户目录（I18）。
 * <ul>
 *   <li>GET /api/v1/admin/users?role= — 列表（可选角色过滤）</li>
 *   <li>POST /api/v1/admin/users/{id}/status — 启停</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/admin/users")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<List<AdminUserResponse>> list(
            @RequestParam(required = false) String role
    ) {
        return ApiResponse.ok(adminUserService.listUsers(role));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<AdminUserResponse> status(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        UserStatus status = UserStatus.valueOf(body.getOrDefault("status", "ENABLED"));
        return ApiResponse.ok(adminUserService.changeStatus(id, status));
    }
}
