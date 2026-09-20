package com.meiyuemall.tenant.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.tenant.dto.OnboardingApplicationResponse;
import com.meiyuemall.tenant.dto.OnboardingApplyRequest;
import com.meiyuemall.tenant.dto.StoreResponse;
import com.meiyuemall.tenant.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家侧入驻 / 店铺 API。
 * <p>
 * 入口：
 * <ul>
 *   <li>{@code POST /api/v1/seller/onboarding/apply} — 提交入驻</li>
 *   <li>{@code GET /api/v1/seller/onboarding/me} — 我的最新申请</li>
 *   <li>{@code GET /api/v1/seller/store} — 我的店铺（需已开店）</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/seller")
public class SellerOnboardingController {

    private final OnboardingService onboardingService;

    public SellerOnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/onboarding/apply")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<OnboardingApplicationResponse> apply(@Valid @RequestBody OnboardingApplyRequest request) {
        return ApiResponse.ok(onboardingService.apply(request));
    }

    @GetMapping("/onboarding/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<OnboardingApplicationResponse> myApplication() {
        return ApiResponse.ok(onboardingService.myLatestApplication());
    }

    @GetMapping("/store")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<StoreResponse> myStore() {
        return ApiResponse.ok(onboardingService.myStore());
    }
}
