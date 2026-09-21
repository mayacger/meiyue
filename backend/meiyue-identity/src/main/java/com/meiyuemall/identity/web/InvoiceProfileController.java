package com.meiyuemall.identity.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.identity.dto.InvoiceProfileResponse;
import com.meiyuemall.identity.dto.InvoiceProfileUpsertRequest;
import com.meiyuemall.identity.service.InvoiceProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 买家发票抬头 API（I33）。
 * <ul>
 *   <li>GET/POST /api/v1/buyer/invoice-profiles</li>
 *   <li>PUT/DELETE /api/v1/buyer/invoice-profiles/{id}</li>
 *   <li>POST /api/v1/buyer/invoice-profiles/{id}/default</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/buyer/invoice-profiles")
@PreAuthorize("isAuthenticated()")
public class InvoiceProfileController {

    private final InvoiceProfileService invoiceProfileService;

    public InvoiceProfileController(InvoiceProfileService invoiceProfileService) {
        this.invoiceProfileService = invoiceProfileService;
    }

    @GetMapping
    public ApiResponse<List<InvoiceProfileResponse>> list() {
        return ApiResponse.ok(invoiceProfileService.listMine());
    }

    @PostMapping
    public ApiResponse<InvoiceProfileResponse> create(@Valid @RequestBody InvoiceProfileUpsertRequest request) {
        return ApiResponse.ok(invoiceProfileService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<InvoiceProfileResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceProfileUpsertRequest request
    ) {
        return ApiResponse.ok(invoiceProfileService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        invoiceProfileService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/default")
    public ApiResponse<InvoiceProfileResponse> setDefault(@PathVariable Long id) {
        return ApiResponse.ok(invoiceProfileService.setDefault(id));
    }
}
