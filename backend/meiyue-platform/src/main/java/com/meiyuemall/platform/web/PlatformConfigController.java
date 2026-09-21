package com.meiyuemall.platform.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.platform.dto.PlatformConfigResponse;
import com.meiyuemall.platform.service.PlatformConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台运营配置公开只读（I23）。
 * <p>GET /api/v1/platform/config — 费率 / 结算周期 / 提现门槛；无密钥、无分账写操作。</p>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/platform")
public class PlatformConfigController {

    private final PlatformConfigService platformConfigService;

    public PlatformConfigController(PlatformConfigService platformConfigService) {
        this.platformConfigService = platformConfigService;
    }

    @GetMapping("/config")
    public ApiResponse<PlatformConfigResponse> config() {
        return ApiResponse.ok(platformConfigService.getPublicConfig());
    }
}
