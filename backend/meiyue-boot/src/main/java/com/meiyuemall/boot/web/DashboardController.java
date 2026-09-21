package com.meiyuemall.boot.web;

import com.meiyuemall.boot.dto.AdminDashboardResponse;
import com.meiyuemall.boot.dto.SellerDashboardResponse;
import com.meiyuemall.boot.service.DashboardAggregateService;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * I20：经营/运营概览 API。
 * <ul>
 *   <li>GET /api/v1/seller/dashboard</li>
 *   <li>GET /api/v1/admin/dashboard</li>
 * </ul>
 */
@RestController
public class DashboardController {

    private final DashboardAggregateService dashboardAggregateService;

    public DashboardController(DashboardAggregateService dashboardAggregateService) {
        this.dashboardAggregateService = dashboardAggregateService;
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/seller/dashboard")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<SellerDashboardResponse> seller() {
        return ApiResponse.ok(dashboardAggregateService.sellerStats());
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/admin/dashboard")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<AdminDashboardResponse> admin() {
        return ApiResponse.ok(dashboardAggregateService.adminStats());
    }
}
