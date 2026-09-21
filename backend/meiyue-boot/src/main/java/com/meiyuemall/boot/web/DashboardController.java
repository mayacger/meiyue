package com.meiyuemall.boot.web;

import com.meiyuemall.boot.dto.AdminDashboardResponse;
import com.meiyuemall.boot.dto.DashboardSeriesResponse;
import com.meiyuemall.boot.dto.SellerDashboardResponse;
import com.meiyuemall.boot.service.DashboardAggregateService;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * I20/I27：经营/运营概览与近 N 日序列。
 * <ul>
 *   <li>GET /api/v1/seller/dashboard</li>
 *   <li>GET /api/v1/seller/dashboard/series?days=7</li>
 *   <li>GET /api/v1/admin/dashboard</li>
 *   <li>GET /api/v1/admin/dashboard/series?days=7</li>
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

    @GetMapping(SecurityConstants.API_PREFIX + "/seller/dashboard/series")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<DashboardSeriesResponse> sellerSeries(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ApiResponse.ok(dashboardAggregateService.sellerSeries(days));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/admin/dashboard")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<AdminDashboardResponse> admin() {
        return ApiResponse.ok(dashboardAggregateService.adminStats());
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/admin/dashboard/series")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<DashboardSeriesResponse> adminSeries(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ApiResponse.ok(dashboardAggregateService.adminSeries(days));
    }
}
