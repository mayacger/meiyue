package com.meiyuemall.catalog.web;

import com.meiyuemall.catalog.domain.CategoryStatus;
import com.meiyuemall.catalog.dto.CategoryResponse;
import com.meiyuemall.catalog.dto.CategoryUpsertRequest;
import com.meiyuemall.catalog.service.CatalogService;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 平台类目管理（I18）。
 * <ul>
 *   <li>GET /api/v1/admin/categories — 全部（含禁用）</li>
 *   <li>POST /api/v1/admin/categories — 创建</li>
 *   <li>PUT /api/v1/admin/categories/{id} — 更新</li>
 *   <li>POST /api/v1/admin/categories/{id}/status — 启停</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/admin/categories")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminCategoryController {

    private final CatalogService catalogService;

    public AdminCategoryController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.ok(catalogService.listAllCategoriesForAdmin());
    }

    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryUpsertRequest request) {
        return ApiResponse.ok(catalogService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpsertRequest request
    ) {
        return ApiResponse.ok(catalogService.updateCategory(id, request));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<CategoryResponse> status(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        CategoryStatus status = CategoryStatus.valueOf(body.getOrDefault("status", "ENABLED"));
        return ApiResponse.ok(catalogService.changeCategoryStatus(id, status));
    }
}
