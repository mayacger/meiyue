package com.meiyuemall.identity.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.identity.dto.AuthResponse;
import com.meiyuemall.identity.dto.ChangePasswordRequest;
import com.meiyuemall.identity.dto.LoginRequest;
import com.meiyuemall.identity.dto.RegisterRequest;
import com.meiyuemall.identity.dto.UpdateProfileRequest;
import com.meiyuemall.identity.dto.UserProfileResponse;
import com.meiyuemall.identity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 / 个人资料 API。
 * <p>
 * 入口：
 * <ul>
 *   <li>{@code POST /api/v1/auth/register} — 注册（公开）</li>
 *   <li>{@code POST /api/v1/auth/login} — 登录（公开）</li>
 *   <li>{@code GET /api/v1/auth/me} — 当前用户</li>
 *   <li>{@code PUT /api/v1/auth/profile} — 更新资料（I24）</li>
 *   <li>{@code POST /api/v1/auth/password} — 修改密码（I24）</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.ok(authService.me());
    }

    /** I24：更新展示名 / 手机 */
    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(authService.updateProfile(request));
    }

    /** I24：修改密码 */
    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok(null);
    }
}
