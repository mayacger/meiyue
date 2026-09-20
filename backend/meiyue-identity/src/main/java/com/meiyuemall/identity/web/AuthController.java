package com.meiyuemall.identity.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.identity.dto.AuthResponse;
import com.meiyuemall.identity.dto.LoginRequest;
import com.meiyuemall.identity.dto.RegisterRequest;
import com.meiyuemall.identity.dto.UserProfileResponse;
import com.meiyuemall.identity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 API。
 * <p>
 * 入口：
 * <ul>
 *   <li>{@code POST /api/v1/auth/register} — 注册（公开）</li>
 *   <li>{@code POST /api/v1/auth/login} — 登录（公开）</li>
 *   <li>{@code GET /api/v1/auth/me} — 当前用户（需登录）</li>
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
}
