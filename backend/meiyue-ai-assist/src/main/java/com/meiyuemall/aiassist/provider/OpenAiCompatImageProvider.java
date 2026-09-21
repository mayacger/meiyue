package com.meiyuemall.aiassist.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * OpenAI 兼容出图占位：未配置 api-key 时明确失败，触发人工上传降级。
 * <p>真实 HTTP 调用留给接密钥后的联调；本实现不入库任何密钥。</p>
 */
@Component
public class OpenAiCompatImageProvider implements AiImageProvider {

    private final String apiKey;

    public OpenAiCompatImageProvider(@Value("${meiyue.ai.openai.api-key:}") String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    @Override
    public String name() {
        return "OPENAI_COMPAT";
    }

    @Override
    public GenerateResult generate(String prompt) {
        if (apiKey.isBlank()) {
            return GenerateResult.fail("AI_OPENAI", "未配置 MEIYUE_AI_API_KEY，请改用 MOCK 或人工上传");
        }
        // 骨架：有密钥也不在无网络联调时强行外呼，避免阻塞；标记需人工或后续接 SDK
        return GenerateResult.fail("AI_OPENAI", "OpenAI 兼容出图骨架未启用外呼，请切 MOCK 或人工上传封面");
    }
}
