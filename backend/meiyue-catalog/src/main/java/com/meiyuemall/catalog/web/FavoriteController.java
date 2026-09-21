package com.meiyuemall.catalog.web;

import com.meiyuemall.catalog.dto.FavoriteResponse;
import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.catalog.service.FavoriteService;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 买家商品收藏 API（I22）。
 * <ul>
 *   <li>POST /api/v1/buyer/favorites/{productId} — 收藏</li>
 *   <li>DELETE /api/v1/buyer/favorites/{productId} — 取消</li>
 *   <li>GET /api/v1/buyer/favorites — 收藏 ID 列表</li>
 *   <li>GET /api/v1/buyer/favorites/products — 收藏商品详情</li>
 *   <li>GET /api/v1/buyer/favorites/{productId}/status — 是否已收藏</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/buyer/favorites")
@PreAuthorize("isAuthenticated()")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{productId}")
    public ApiResponse<FavoriteResponse> add(@PathVariable Long productId) {
        return ApiResponse.ok(favoriteService.add(productId));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> remove(@PathVariable Long productId) {
        favoriteService.remove(productId);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<List<FavoriteResponse>> list() {
        return ApiResponse.ok(favoriteService.listMine());
    }

    @GetMapping("/products")
    public ApiResponse<List<ProductResponse>> listProducts() {
        return ApiResponse.ok(favoriteService.listMineProducts());
    }

    @GetMapping("/{productId}/status")
    public ApiResponse<Map<String, Boolean>> status(@PathVariable Long productId) {
        return ApiResponse.ok(Map.of("favorited", favoriteService.isFavorited(productId)));
    }
}
