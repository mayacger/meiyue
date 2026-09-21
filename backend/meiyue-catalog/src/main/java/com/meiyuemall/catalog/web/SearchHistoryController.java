package com.meiyuemall.catalog.web;

import com.meiyuemall.catalog.service.SearchHistoryService;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 买家搜索历史 API（I32）。
 * <ul>
 *   <li>POST /api/v1/buyer/search-history — body: { keyword }</li>
 *   <li>GET  /api/v1/buyer/search-history</li>
 *   <li>DELETE /api/v1/buyer/search-history</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/buyer/search-history")
@PreAuthorize("isAuthenticated()")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    public SearchHistoryController(SearchHistoryService searchHistoryService) {
        this.searchHistoryService = searchHistoryService;
    }

    @PostMapping
    public ApiResponse<Void> record(@RequestBody Map<String, String> body) {
        searchHistoryService.record(body == null ? null : body.get("keyword"));
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<List<String>> list() {
        return ApiResponse.ok(searchHistoryService.listMine());
    }

    @DeleteMapping
    public ApiResponse<Void> clear() {
        searchHistoryService.clearMine();
        return ApiResponse.ok(null);
    }
}
