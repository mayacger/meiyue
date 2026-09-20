package com.meiyuemall.aftersale.web;

import com.meiyuemall.aftersale.dto.AftersaleResponse;
import com.meiyuemall.aftersale.dto.ApplyAftersaleRequest;
import com.meiyuemall.aftersale.dto.FillReverseTrackingRequest;
import com.meiyuemall.aftersale.dto.ReviewAftersaleRequest;
import com.meiyuemall.aftersale.service.AftersaleService;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 售后 API（I6）。
 */
@RestController
public class AftersaleController {

    private final AftersaleService aftersaleService;

    public AftersaleController(AftersaleService aftersaleService) {
        this.aftersaleService = aftersaleService;
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/buyer/aftersales")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AftersaleResponse> apply(@Valid @RequestBody ApplyAftersaleRequest request) {
        return ApiResponse.ok(aftersaleService.apply(request));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/buyer/aftersales")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AftersaleResponse>> buyerList() {
        return ApiResponse.ok(aftersaleService.listMineBuyer());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/buyer/aftersales/{id}/reverse-tracking")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AftersaleResponse> reverseTracking(
            @PathVariable Long id,
            @Valid @RequestBody FillReverseTrackingRequest request
    ) {
        return ApiResponse.ok(aftersaleService.fillReverseTracking(id, request));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/seller/aftersales")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<List<AftersaleResponse>> sellerList() {
        return ApiResponse.ok(aftersaleService.listMineSeller());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/aftersales/{id}/approve")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<AftersaleResponse> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewAftersaleRequest request
    ) {
        return ApiResponse.ok(aftersaleService.approve(id, request == null ? new ReviewAftersaleRequest(null) : request, false));
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/aftersales/{id}/reject")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<AftersaleResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewAftersaleRequest request
    ) {
        return ApiResponse.ok(aftersaleService.reject(id, request == null ? new ReviewAftersaleRequest(null) : request));
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/aftersales/{id}/confirm-return")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<AftersaleResponse> confirmReturn(@PathVariable Long id) {
        return ApiResponse.ok(aftersaleService.confirmReturnReceived(id));
    }
}
