package com.meiyuemall.payment.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.payment.dto.AdminSettlementPeriodSummary;
import com.meiyuemall.payment.dto.SettlementLedgerResponse;
import com.meiyuemall.payment.service.SettlementLedgerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台结算只读汇总（I28）。
 * <p>无真实分账打款；仅账本聚合。</p>
 * <ul>
 *   <li>GET /api/v1/admin/settlements/periods</li>
 *   <li>GET /api/v1/admin/settlements/bills?tenantId=&amp;periodKey=</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/admin/settlements")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminSettlementController {

    private final SettlementLedgerService settlementLedgerService;

    public AdminSettlementController(SettlementLedgerService settlementLedgerService) {
        this.settlementLedgerService = settlementLedgerService;
    }

    @GetMapping("/periods")
    public ApiResponse<List<AdminSettlementPeriodSummary>> periods() {
        return ApiResponse.ok(settlementLedgerService.adminPeriodSummaries());
    }

    @GetMapping("/bills")
    public ApiResponse<List<SettlementLedgerResponse>> bills(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String periodKey
    ) {
        return ApiResponse.ok(settlementLedgerService.adminListBills(tenantId, periodKey));
    }
}
