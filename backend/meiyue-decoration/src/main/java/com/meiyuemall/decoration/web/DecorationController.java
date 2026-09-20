package com.meiyuemall.decoration.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.decoration.dto.SaveDraftRequest;
import com.meiyuemall.decoration.dto.StorePageResponse;
import com.meiyuemall.decoration.dto.TemplateResponse;
import com.meiyuemall.decoration.service.DecorationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 装修 API。
 * <ul>
 *   <li>GET /api/v1/decoration/templates — 模板列表（登录可）</li>
 *   <li>商家草稿/发布：/api/v1/seller/decoration/**</li>
 *   <li>GET /api/v1/stores/{tenantId}/page — 已发布页（公开）</li>
 * </ul>
 */
@RestController
public class DecorationController {

    private final DecorationService decorationService;

    public DecorationController(DecorationService decorationService) {
        this.decorationService = decorationService;
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/decoration/templates")
    public ApiResponse<List<TemplateResponse>> templates() {
        return ApiResponse.ok(decorationService.listTemplates());
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/seller/decoration/draft")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<StorePageResponse> draft() {
        return ApiResponse.ok(decorationService.getDraft());
    }

    @PutMapping(SecurityConstants.API_PREFIX + "/seller/decoration/draft")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<StorePageResponse> saveDraft(@Valid @RequestBody SaveDraftRequest request) {
        return ApiResponse.ok(decorationService.saveDraft(request));
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/decoration/publish")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<StorePageResponse> publish() {
        return ApiResponse.ok(decorationService.publish());
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/stores/{tenantId}/page")
    public ApiResponse<StorePageResponse> published(@PathVariable Long tenantId) {
        return ApiResponse.ok(decorationService.getPublished(tenantId));
    }
}
