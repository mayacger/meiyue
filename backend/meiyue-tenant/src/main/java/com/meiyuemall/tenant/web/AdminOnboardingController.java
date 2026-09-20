package com.meiyuemall.tenant.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.tenant.dto.OnboardingApplicationResponse;
import com.meiyuemall.tenant.dto.ReviewOnboardingRequest;
import com.meiyuemall.tenant.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台侧入驻审核 API。
 * <p>
 * 入口：
 * <ul>
 *   <li>{@code GET /api/v1/admin/onboarding/pending} — 待审列表</li>
 *   <li>{@code POST /api/v1/admin/onboarding/{id}/approve} — 通过并开店</li>
 *   <li>{@code POST /api/v1/admin/onboarding/{id}/reject} — 拒绝</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/admin/onboarding")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminOnboardingController {

    private final OnboardingService onboardingService;

    public AdminOnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping("/pending")
    public ApiResponse<List<OnboardingApplicationResponse>> pending() {
        return ApiResponse.ok(onboardingService.listPending());
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<OnboardingApplicationResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ReviewOnboardingRequest request
    ) {
        return ApiResponse.ok(onboardingService.approve(id, request == null ? new ReviewOnboardingRequest(null) : request));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<OnboardingApplicationResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ReviewOnboardingRequest request
    ) {
        return ApiResponse.ok(onboardingService.reject(id, request == null ? new ReviewOnboardingRequest(null) : request));
    }
}
