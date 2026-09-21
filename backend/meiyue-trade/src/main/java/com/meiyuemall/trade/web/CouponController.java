package com.meiyuemall.trade.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.trade.dto.CouponClaimResponse;
import com.meiyuemall.trade.dto.CreateStoreCouponRequest;
import com.meiyuemall.trade.dto.StoreCouponResponse;
import com.meiyuemall.trade.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 店券 API（商家发券 / 买家领用） */
@RestController
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/coupons")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<StoreCouponResponse> create(@Valid @RequestBody CreateStoreCouponRequest request) {
        return ApiResponse.ok(couponService.create(request));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/seller/coupons")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<List<StoreCouponResponse>> sellerList() {
        return ApiResponse.ok(couponService.listMineSeller());
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/stores/{tenantId}/coupons")
    public ApiResponse<List<StoreCouponResponse>> storeCoupons(@PathVariable Long tenantId) {
        return ApiResponse.ok(couponService.listActiveByTenant(tenantId));
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/buyer/coupons/{couponId}/claim")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CouponClaimResponse> claim(@PathVariable Long couponId) {
        return ApiResponse.ok(couponService.claim(couponId));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/buyer/coupons/claims")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CouponClaimResponse>> myClaims() {
        return ApiResponse.ok(couponService.listMineBuyer());
    }
}
