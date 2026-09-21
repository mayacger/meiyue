package com.meiyuemall.tenant.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.tenant.dto.InviteStaffRequest;
import com.meiyuemall.tenant.dto.StaffMemberResponse;
import com.meiyuemall.tenant.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家员工管理（I26 · 仅店主）。
 * <ul>
 *   <li>GET /api/v1/seller/staff — 本店成员</li>
 *   <li>POST /api/v1/seller/staff/invite — 邀请店员</li>
 *   <li>DELETE /api/v1/seller/staff/{memberId} — 移除店员</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/seller/staff")
@PreAuthorize("hasRole('SELLER_OWNER')")
public class SellerStaffController {

    private final StaffService staffService;

    public SellerStaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ApiResponse<List<StaffMemberResponse>> list() {
        return ApiResponse.ok(staffService.listMine());
    }

    @PostMapping("/invite")
    public ApiResponse<StaffMemberResponse> invite(@Valid @RequestBody InviteStaffRequest request) {
        return ApiResponse.ok(staffService.invite(request));
    }

    @DeleteMapping("/{memberId}")
    public ApiResponse<Void> remove(@PathVariable Long memberId) {
        staffService.remove(memberId);
        return ApiResponse.ok(null);
    }
}
