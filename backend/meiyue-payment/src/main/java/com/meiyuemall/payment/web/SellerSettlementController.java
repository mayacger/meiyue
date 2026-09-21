package com.meiyuemall.payment.web;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.payment.dto.SettlementLedgerResponse;
import com.meiyuemall.payment.dto.SettlementPeriodSummary;
import com.meiyuemall.payment.service.SettlementLedgerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商家结算账单（MVP 记账；不接官方分账打款）。
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/seller/settlements")
@PreAuthorize("hasRole('SELLER_OWNER')")
public class SellerSettlementController {

    private final SettlementLedgerService settlementLedgerService;

    public SellerSettlementController(SettlementLedgerService settlementLedgerService) {
        this.settlementLedgerService = settlementLedgerService;
    }

    @GetMapping("/bills")
    public ApiResponse<List<SettlementLedgerResponse>> bills(
            @RequestParam(required = false) String periodKey
    ) {
        return ApiResponse.ok(settlementLedgerService.listBills(requireTenant(), periodKey));
    }

    @GetMapping("/periods")
    public ApiResponse<List<SettlementPeriodSummary>> periods() {
        return ApiResponse.ok(settlementLedgerService.periodSummaries(requireTenant()));
    }

    private Long requireTenant() {
        Long tenantId = SecurityUtils.requirePrincipal().getTenantId();
        if (tenantId == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED);
        }
        return tenantId;
    }
}
