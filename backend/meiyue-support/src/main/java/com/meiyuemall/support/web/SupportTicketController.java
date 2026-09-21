package com.meiyuemall.support.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.support.dto.CreateTicketRequest;
import com.meiyuemall.support.dto.TicketResponse;
import com.meiyuemall.support.service.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 客服工单 API（I21 MVP，非 IM）。
 * <ul>
 *   <li>POST /api/v1/buyer/tickets — 买家开单</li>
 *   <li>GET /api/v1/buyer/tickets — 我的工单</li>
 *   <li>GET /api/v1/seller/tickets — 本店工单</li>
 *   <li>POST /api/v1/seller/tickets/{id}/reply</li>
 *   <li>GET /api/v1/admin/tickets — 平台全部</li>
 *   <li>POST /api/v1/admin/tickets/{id}/reply</li>
 *   <li>POST .../tickets/{id}/close — 买家/商家/平台关闭</li>
 * </ul>
 */
@RestController
public class SupportTicketController {

    private final SupportTicketService ticketService;

    public SupportTicketController(SupportTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/buyer/tickets")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        return ApiResponse.ok(ticketService.create(request));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/buyer/tickets")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<TicketResponse>> mine() {
        return ApiResponse.ok(ticketService.listMine());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/buyer/tickets/{id}/close")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TicketResponse> buyerClose(@PathVariable Long id) {
        return ApiResponse.ok(ticketService.close(id));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/seller/tickets")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<List<TicketResponse>> sellerList() {
        return ApiResponse.ok(ticketService.listForSeller());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/tickets/{id}/reply")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<TicketResponse> sellerReply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(ticketService.sellerReply(id, body.getOrDefault("reply", "")));
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/tickets/{id}/close")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<TicketResponse> sellerClose(@PathVariable Long id) {
        return ApiResponse.ok(ticketService.close(id));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/admin/tickets")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<List<TicketResponse>> adminList() {
        return ApiResponse.ok(ticketService.listForAdmin());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/admin/tickets/{id}/reply")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<TicketResponse> adminReply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(ticketService.adminReply(id, body.getOrDefault("reply", "")));
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/admin/tickets/{id}/close")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<TicketResponse> adminClose(@PathVariable Long id) {
        return ApiResponse.ok(ticketService.close(id));
    }
}
