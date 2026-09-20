package com.meiyuemall.trade.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.trade.dto.CreatePlatformCouponRequest;
import com.meiyuemall.trade.dto.PlatformCouponClaimResponse;
import com.meiyuemall.trade.dto.PlatformCouponResponse;
import com.meiyuemall.trade.service.PlatformCouponService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台券 API。
 * <p>与店券互斥规则：下单不可同时传 store/platform claim（MUTUAL_EXCLUSIVE）。</p>
 */
@RestController
public class PlatformCouponController {

    private final PlatformCouponService platformCouponService;

    public PlatformCouponController(PlatformCouponService platformCouponService) {
        this.platformCouponService = platformCouponService;
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/admin/platform-coupons")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<PlatformCouponResponse> create(@Valid @RequestBody CreatePlatformCouponRequest request) {
        return ApiResponse.ok(platformCouponService.create(request));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/platform-coupons")
    public ApiResponse<List<PlatformCouponResponse>> listActive() {
        return ApiResponse.ok(platformCouponService.listActive());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/buyer/platform-coupons/{couponId}/claim")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PlatformCouponClaimResponse> claim(@PathVariable Long couponId) {
        return ApiResponse.ok(platformCouponService.claim(couponId));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/buyer/platform-coupons/claims")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PlatformCouponClaimResponse>> myClaims() {
        return ApiResponse.ok(platformCouponService.listMine());
    }
}
