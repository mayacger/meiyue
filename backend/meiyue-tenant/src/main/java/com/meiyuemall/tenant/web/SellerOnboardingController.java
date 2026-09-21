package com.meiyuemall.tenant.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.tenant.dto.OnboardingApplicationResponse;
import com.meiyuemall.tenant.dto.OnboardingApplyRequest;
import com.meiyuemall.tenant.dto.StoreResponse;
import com.meiyuemall.tenant.dto.StoreUpdateRequest;
import com.meiyuemall.tenant.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家侧入驻 / 店铺 API。
 * <ul>
 *   <li>POST /seller/onboarding/apply</li>
 *   <li>GET /seller/onboarding/me</li>
 *   <li>GET/PUT /seller/store — 店铺资料（I22 可更新）</li>
 * </ul>
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

    /** I22：更新店名/简介/Logo */
    @PutMapping("/store")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<StoreResponse> updateStore(@Valid @RequestBody StoreUpdateRequest request) {
        return ApiResponse.ok(onboardingService.updateMyStore(request));
    }
}
