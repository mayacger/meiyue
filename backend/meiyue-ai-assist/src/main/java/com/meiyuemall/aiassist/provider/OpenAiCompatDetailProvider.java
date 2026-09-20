package com.meiyuemall.aiassist.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * OpenAI 兼容详情占位：无密钥 → 失败降级。
 */
@Component
public class OpenAiCompatDetailProvider implements AiDetailProvider {

    private final String apiKey;

    public OpenAiCompatDetailProvider(@Value("${meiyue.ai.openai.api-key:}") String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    @Override
    public String name() {
        return "OPENAI_COMPAT";
    }

    @Override
    public DetailResult generate(String title, String hints) {
        if (apiKey.isBlank()) {
            return DetailResult.fail("AI_OPENAI", "未配置 MEIYUE_AI_API_KEY，请改用 MOCK 或人工填写详情");
        }
        return DetailResult.fail("AI_OPENAI", "OpenAI 兼容详情骨架未启用外呼，请切 MOCK 或人工填写");
    }
}
