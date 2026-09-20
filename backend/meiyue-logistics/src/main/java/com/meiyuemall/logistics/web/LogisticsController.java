package com.meiyuemall.logistics.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.logistics.dto.CreateForwardShipmentRequest;
import com.meiyuemall.logistics.dto.ShipmentResponse;
import com.meiyuemall.logistics.dto.UpdateShipmentStatusRequest;
import com.meiyuemall.logistics.service.LogisticsService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家物流 API + 买家查询。
 */
@RestController
public class LogisticsController {

    private final LogisticsService logisticsService;

    public LogisticsController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/shipments")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<ShipmentResponse> create(@Valid @RequestBody CreateForwardShipmentRequest request) {
        return ApiResponse.ok(logisticsService.createForward(request));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/seller/shipments")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<List<ShipmentResponse>> mine() {
        return ApiResponse.ok(logisticsService.listMineForward());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/shipments/{id}/ewaybill/print")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<ShipmentResponse> printEwaybill(@PathVariable Long id) {
        return ApiResponse.ok(logisticsService.printEwaybill(id));
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/shipments/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<ShipmentResponse> status(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShipmentStatusRequest request
    ) {
        return ApiResponse.ok(logisticsService.updateStatus(id, request));
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/shipments/{id}/sync-tracks")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<ShipmentResponse> sync(@PathVariable Long id) {
        return ApiResponse.ok(logisticsService.syncTracks(id));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/buyer/orders/{orderId}/shipments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ShipmentResponse>> byOrder(@PathVariable Long orderId) {
        return ApiResponse.ok(logisticsService.listByOrder(orderId));
    }
}
