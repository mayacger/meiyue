package com.meiyuemall.catalog.web;

import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.catalog.service.CatalogService;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * I36：平台商品回收站治理。
 * <ul>
 *   <li>GET /api/v1/admin/products/deleted — 全站已软删列表</li>
 *   <li>POST /api/v1/admin/products/{id}/restore — 代恢复</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/admin/products")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminProductController {

    private final CatalogService catalogService;

    public AdminProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/deleted")
    public ApiResponse<List<ProductResponse>> deleted() {
        return ApiResponse.ok(catalogService.adminListDeleted());
    }

    @PostMapping("/{id}/restore")
    public ApiResponse<ProductResponse> restore(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.adminRestore(id));
    }
}
