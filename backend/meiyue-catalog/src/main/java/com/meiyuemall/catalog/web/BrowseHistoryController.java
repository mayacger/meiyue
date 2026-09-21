package com.meiyuemall.catalog.web;

import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.catalog.service.BrowseHistoryService;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 买家浏览足迹（I30）。
 * <ul>
 *   <li>POST /api/v1/buyer/browse-history/{productId} — 记录</li>
 *   <li>GET  /api/v1/buyer/browse-history — 列表（商品）</li>
 *   <li>DELETE /api/v1/buyer/browse-history — 清空</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/buyer/browse-history")
@PreAuthorize("isAuthenticated()")
public class BrowseHistoryController {

    private final BrowseHistoryService browseHistoryService;

    public BrowseHistoryController(BrowseHistoryService browseHistoryService) {
        this.browseHistoryService = browseHistoryService;
    }

    @PostMapping("/{productId}")
    public ApiResponse<Void> record(@PathVariable Long productId) {
        browseHistoryService.record(productId);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> list() {
        return ApiResponse.ok(browseHistoryService.listMineProducts());
    }

    @DeleteMapping
    public ApiResponse<Void> clear() {
        browseHistoryService.clearMine();
        return ApiResponse.ok(null);
    }
}
