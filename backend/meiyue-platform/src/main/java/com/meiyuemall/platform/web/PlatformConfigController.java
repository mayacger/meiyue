package com.meiyuemall.platform.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.platform.dto.PlatformConfigResponse;
import com.meiyuemall.platform.service.PlatformConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 平台运营配置（I23 公开只读 + I32 Admin 更新）。
 * <ul>
 *   <li>GET /api/v1/platform/config</li>
 *   <li>PUT /api/v1/admin/platform-config — body: { key, value }</li>
 * </ul>
 */
@RestController
public class PlatformConfigController {

    private final PlatformConfigService platformConfigService;

    public PlatformConfigController(PlatformConfigService platformConfigService) {
        this.platformConfigService = platformConfigService;
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/platform/config")
    public ApiResponse<PlatformConfigResponse> config() {
        return ApiResponse.ok(platformConfigService.getPublicConfig());
    }

    @PutMapping(SecurityConstants.API_PREFIX + "/admin/platform-config")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<PlatformConfigResponse> adminUpdate(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(platformConfigService.update(
                body.get("key"),
                body.get("value")
        ));
    }
}
