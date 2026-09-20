package com.meiyuemall.aiassist.provider;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * MOCK 推广视频：立即返回占位 URL（异步任务中调用）。
 */
@Component
public class MockAiVideoProvider implements AiVideoProvider {

    @Override
    public String name() {
        return "MOCK";
    }

    @Override
    public VideoResult generate(String prompt) {
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        // 明确非直播：promo video placeholder
        String url = "mock://promo-video/" + id + ".mp4";
        return new VideoResult(true, "AI_MOCK", url, null);
    }
}
