package com.meiyuemall.tenant.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.tenant.dto.StoreResponse;
import com.meiyuemall.tenant.service.OnboardingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开店铺资料（I22）。
 * <ul>
 *   <li>GET /api/v1/stores/{tenantId}</li>
 *   <li>GET /api/v1/stores/slug/{slug}</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/stores")
public class PublicStoreController {

    private final OnboardingService onboardingService;

    public PublicStoreController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping("/{tenantId}")
    public ApiResponse<StoreResponse> byTenant(@PathVariable Long tenantId) {
        return ApiResponse.ok(onboardingService.getPublicByTenant(tenantId));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<StoreResponse> bySlug(@PathVariable String slug) {
        return ApiResponse.ok(onboardingService.getPublicBySlug(slug));
    }
}
