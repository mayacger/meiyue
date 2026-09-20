package com.meiyuemall.aiassist.config;

import com.meiyuemall.aiassist.provider.AiDetailProvider;
import com.meiyuemall.aiassist.provider.AiImageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按配置选择当前 AI Provider；默认 MOCK。
 */
@Component
public class AiProviderRegistry {

    private final Map<String, AiImageProvider> images;
    private final Map<String, AiDetailProvider> details;
    private final String active;

    public AiProviderRegistry(
            List<AiImageProvider> imageProviders,
            List<AiDetailProvider> detailProviders,
            @Value("${meiyue.ai.provider:MOCK}") String active
    ) {
        this.images = imageProviders.stream()
                .collect(Collectors.toMap(p -> p.name().toUpperCase(), Function.identity(), (a, b) -> a));
        this.details = detailProviders.stream()
                .collect(Collectors.toMap(p -> p.name().toUpperCase(), Function.identity(), (a, b) -> a));
        this.active = active == null ? "MOCK" : active.trim().toUpperCase();
    }

    public AiImageProvider imageProvider() {
        AiImageProvider p = images.get(active);
        if (p == null) {
            p = images.get("MOCK");
        }
        if (p == null) {
            throw new IllegalStateException("无可用 AiImageProvider");
        }
        return p;
    }

    public AiDetailProvider detailProvider() {
        AiDetailProvider p = details.get(active);
        if (p == null) {
            p = details.get("MOCK");
        }
        if (p == null) {
            throw new IllegalStateException("无可用 AiDetailProvider");
        }
        return p;
    }

    public String activeName() {
        return active;
    }
}
