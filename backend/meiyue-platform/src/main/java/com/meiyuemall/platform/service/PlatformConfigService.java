package com.meiyuemall.platform.service;

import com.meiyuemall.platform.domain.PlatformConfig;
import com.meiyuemall.platform.dto.PlatformConfigResponse;
import com.meiyuemall.platform.repo.PlatformConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 平台运营配置只读服务（无写接口、无真实分账）。
 */
@Service
public class PlatformConfigService {

    private final PlatformConfigRepository repository;

    public PlatformConfigService(PlatformConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PlatformConfigResponse getPublicConfig() {
        List<PlatformConfig> all = repository.findAllByOrderByConfigKeyAsc();
        Map<String, String> map = all.stream()
                .collect(Collectors.toMap(PlatformConfig::getConfigKey, PlatformConfig::getConfigValue, (a, b) -> a));
        List<PlatformConfigResponse.PlatformConfigItem> items = all.stream()
                .map(c -> new PlatformConfigResponse.PlatformConfigItem(
                        c.getConfigKey(),
                        c.getConfigValue(),
                        c.getDescription(),
                        c.getUpdatedAt() == null ? null : c.getUpdatedAt().toString()
                ))
                .toList();
        return new PlatformConfigResponse(
                items,
                map.getOrDefault("platform_fee_rate_bps", "50"),
                map.getOrDefault("settlement_cycle", "WEEKLY"),
                map.getOrDefault("min_withdraw_cents", "10000")
        );
    }
}
