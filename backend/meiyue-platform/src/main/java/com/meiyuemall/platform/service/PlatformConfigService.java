package com.meiyuemall.platform.service;

import com.meiyuemall.common.audit.Audited;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.platform.domain.PlatformConfig;
import com.meiyuemall.platform.dto.PlatformConfigResponse;
import com.meiyuemall.platform.repo.PlatformConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 平台运营配置（I23 只读展示 + I32 可改自动确认天数）。
 */
@Service
public class PlatformConfigService {

    public static final String KEY_AUTO_CONFIRM_DAYS = "auto_confirm_receipt_days";

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
                map.getOrDefault("min_withdraw_cents", "10000"),
                map.getOrDefault(KEY_AUTO_CONFIRM_DAYS, "7")
        );
    }

    /** 读取整型配置；非法或缺失时回退默认值 */
    @Transactional(readOnly = true)
    public int getInt(String key, int defaultValue) {
        return repository.findById(key)
                .map(c -> {
                    try {
                        return Integer.parseInt(c.getConfigValue().trim());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    /**
     * Admin 更新配置项（I32：主要用于 auto_confirm_receipt_days）。
     */
    @Transactional
    @Audited(action = "PLATFORM_CONFIG_UPDATE", resourceType = "PlatformConfig")
    public PlatformConfigResponse update(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "configKey 不能为空");
        }
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "configValue 不能为空");
        }
        PlatformConfig row = repository.findById(key.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "配置不存在: " + key));
        row.setConfigValue(value.trim());
        return getPublicConfig();
    }
}
