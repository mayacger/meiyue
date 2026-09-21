package com.meiyuemall.catalog.web;

import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.dto.BatchProductStatusRequest;
import com.meiyuemall.catalog.dto.CategoryResponse;
import com.meiyuemall.catalog.dto.InventorySkuResponse;
import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.catalog.dto.ProductUpsertRequest;
import com.meiyuemall.catalog.dto.StockAdjustRequest;
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
    public ApiResponse<List<ProductResponse>> onSaleProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId
    ) {
        if ((q != null && !q.isBlank()) || categoryId != null) {
            return ApiResponse.ok(catalogService.searchOnSale(q, categoryId));
        }
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

    /** I25：批量上下架 */
    @PostMapping(SecurityConstants.API_PREFIX + "/seller/products/batch-status")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<List<ProductResponse>> batchStatus(@Valid @RequestBody BatchProductStatusRequest request) {
        return ApiResponse.ok(catalogService.batchChangeStatus(request));
    }

    /** I20：本店 SKU 库存列表 */
    @GetMapping(SecurityConstants.API_PREFIX + "/seller/inventory")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<List<InventorySkuResponse>> inventory() {
        return ApiResponse.ok(catalogService.listInventory());
    }

    /** I25：库存预警列表（默认阈值 5） */
    @GetMapping(SecurityConstants.API_PREFIX + "/seller/inventory/alerts")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<List<InventorySkuResponse>> inventoryAlerts(
            @RequestParam(defaultValue = "5") int threshold
    ) {
        return ApiResponse.ok(catalogService.listLowStock(threshold));
    }

    /** I20：调整 SKU 库存为目标值 */
    @PostMapping(SecurityConstants.API_PREFIX + "/seller/inventory/skus/{skuId}/stock")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<InventorySkuResponse> adjustStock(
            @PathVariable Long skuId,
            @Valid @RequestBody StockAdjustRequest request
    ) {
        return ApiResponse.ok(catalogService.adjustStock(skuId, request));
    }
}
