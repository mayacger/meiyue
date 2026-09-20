package com.meiyuemall.aiassist.config;

import com.meiyuemall.aiassist.provider.AiDetailProvider;
import com.meiyuemall.aiassist.provider.AiImageProvider;
import com.meiyuemall.aiassist.provider.AiVideoProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按配置选择当前 AI Provider；默认 MOCK。
 * <p>视频通道现阶段强制 MOCK（真实模型二期后置）。</p>
 */
@Component
public class AiProviderRegistry {

    private final Map<String, AiImageProvider> images;
    private final Map<String, AiDetailProvider> details;
    private final Map<String, AiVideoProvider> videos;
    private final String active;

    public AiProviderRegistry(
            List<AiImageProvider> imageProviders,
            List<AiDetailProvider> detailProviders,
            List<AiVideoProvider> videoProviders,
            @Value("${meiyue.ai.provider:MOCK}") String active
    ) {
        this.images = imageProviders.stream()
                .collect(Collectors.toMap(p -> p.name().toUpperCase(), Function.identity(), (a, b) -> a));
        this.details = detailProviders.stream()
                .collect(Collectors.toMap(p -> p.name().toUpperCase(), Function.identity(), (a, b) -> a));
        this.videos = videoProviders.stream()
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

    /** 推广视频：当前仅 MOCK */
    public AiVideoProvider videoProvider() {
        AiVideoProvider p = videos.get("MOCK");
        if (p == null && !videos.isEmpty()) {
            p = videos.values().iterator().next();
        }
        if (p == null) {
            throw new IllegalStateException("无可用 AiVideoProvider");
        }
        return p;
    }

    public String activeName() {
        return active;
    }
}
