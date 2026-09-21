package com.meiyuemall.boot.web;

import com.meiyuemall.boot.dto.AdminDashboardResponse;
import com.meiyuemall.boot.dto.DashboardSeriesResponse;
import com.meiyuemall.boot.dto.SellerDashboardResponse;
import com.meiyuemall.boot.service.DashboardAggregateService;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * I20/I27/I36：经营/运营概览、近 N 日序列、销售报表 CSV。
 * <ul>
 *   <li>GET /api/v1/seller/dashboard</li>
 *   <li>GET /api/v1/seller/dashboard/series?days=7</li>
 *   <li>GET /api/v1/seller/dashboard/sales-report.csv?grain=day|week&amp;periods=7</li>
 *   <li>GET /api/v1/admin/dashboard</li>
 *   <li>GET /api/v1/admin/dashboard/series?days=7</li>
 *   <li>GET /api/v1/admin/dashboard/sales-report.csv?grain=day|week&amp;periods=7</li>
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

    /** I36：商家销售报表 CSV */
    @GetMapping(value = SecurityConstants.API_PREFIX + "/seller/dashboard/sales-report.csv",
            produces = "text/csv;charset=UTF-8")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ResponseEntity<byte[]> sellerSalesReport(
            @RequestParam(defaultValue = "day") String grain,
            @RequestParam(defaultValue = "7") int periods
    ) {
        String csv = dashboardAggregateService.exportSellerSalesCsv(grain, periods);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"seller-sales-report.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
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

    /** I36：平台销售汇总 CSV */
    @GetMapping(value = SecurityConstants.API_PREFIX + "/admin/dashboard/sales-report.csv",
            produces = "text/csv;charset=UTF-8")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<byte[]> adminSalesReport(
            @RequestParam(defaultValue = "day") String grain,
            @RequestParam(defaultValue = "7") int periods
    ) {
        String csv = dashboardAggregateService.exportAdminSalesCsv(grain, periods);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"admin-sales-report.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
