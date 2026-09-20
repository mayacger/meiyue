package com.meiyuemall.catalog.web;

import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.dto.CategoryResponse;
import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.catalog.dto.ProductUpsertRequest;
import com.meiyuemall.catalog.service.CatalogService;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商家商品 API + 公开类目 / 买家浏览。
 * <ul>
 *   <li>GET /api/v1/categories — 公开</li>
 *   <li>GET /api/v1/products — 已上架列表（公开）</li>
 *   <li>GET /api/v1/products/{id} — 已上架详情（公开）</li>
 *   <li>GET /api/v1/stores/{tenantId}/products — 某店已上架（公开）</li>
 *   <li>/api/v1/seller/products/** — 商家管理</li>
 * </ul>
 */
@RestController
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/categories")
    public ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.ok(catalogService.listCategories());
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/products")
    public ApiResponse<List<ProductResponse>> onSaleProducts() {
        return ApiResponse.ok(catalogService.listOnSale());
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/products/{id}")
    public ApiResponse<ProductResponse> onSaleDetail(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.getOnSale(id));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/stores/{tenantId}/products")
    public ApiResponse<List<ProductResponse>> storeProducts(@PathVariable Long tenantId) {
        return ApiResponse.ok(catalogService.listOnSaleByTenant(tenantId));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/seller/products")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<List<ProductResponse>> mine() {
        return ApiResponse.ok(catalogService.listMine());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/products")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductUpsertRequest request) {
        return ApiResponse.ok(catalogService.create(request));
    }

    @PutMapping(SecurityConstants.API_PREFIX + "/seller/products/{id}")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductUpsertRequest request) {
        return ApiResponse.ok(catalogService.update(id, request));
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/products/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<ProductResponse> status(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ProductStatus status = ProductStatus.valueOf(body.getOrDefault("status", "DRAFT"));
        return ApiResponse.ok(catalogService.changeStatus(id, status));
    }
}
