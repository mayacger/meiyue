package com.meiyuemall.aiassist.provider;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 本地 MOCK 出图：始终成功，返回 mock:// 占位 URL，便于无密钥联调。
 */
@Component
public class MockAiImageProvider implements AiImageProvider {

    @Override
    public String name() {
        return "MOCK";
    }

    @Override
    public GenerateResult generate(String prompt) {
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String url = "mock://ai-image/" + id + "?q=" + (prompt == null ? "" : prompt.hashCode());
        return GenerateResult.ok(url, "AI_MOCK");
    }
}
