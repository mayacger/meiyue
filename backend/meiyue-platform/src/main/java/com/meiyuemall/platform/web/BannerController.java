package com.meiyuemall.platform.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.platform.dto.BannerResponse;
import com.meiyuemall.platform.dto.BannerUpsertRequest;
import com.meiyuemall.platform.service.BannerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台 Banner API（I28）。
 * <ul>
 *   <li>GET /api/v1/banners — 公开当前可展示</li>
 *   <li>GET/POST/PUT/DELETE /api/v1/admin/banners — 平台 CRUD</li>
 * </ul>
 */
@RestController
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/banners")
    public ApiResponse<List<BannerResponse>> active() {
        return ApiResponse.ok(bannerService.listActive());
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/admin/banners")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<List<BannerResponse>> adminList() {
        return ApiResponse.ok(bannerService.listAll());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/admin/banners")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<BannerResponse> create(@Valid @RequestBody BannerUpsertRequest request) {
        return ApiResponse.ok(bannerService.create(request));
    }

    @PutMapping(SecurityConstants.API_PREFIX + "/admin/banners/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<BannerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BannerUpsertRequest request
    ) {
        return ApiResponse.ok(bannerService.update(id, request));
    }

    @DeleteMapping(SecurityConstants.API_PREFIX + "/admin/banners/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ApiResponse.ok(null);
    }
}
