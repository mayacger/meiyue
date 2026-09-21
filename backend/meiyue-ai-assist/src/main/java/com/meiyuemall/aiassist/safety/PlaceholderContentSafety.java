package com.meiyuemall.aiassist.safety;

import org.springframework.stereotype.Component;

/**
 * 内容安全占位：命中简单违禁词则拒绝，其余通过。
 */
@Component
public class PlaceholderContentSafety implements ContentSafetyPort {

    private static final String[] BLOCK = {"违禁", "赌博", "毒品"};

    @Override
    public SafetyResult reviewText(String text) {
        return scan(text, "TEXT_PLACEHOLDER");
    }

    @Override
    public SafetyResult reviewImageUrl(String url, String prompt) {
        return scan(prompt, "IMAGE_PLACEHOLDER");
    }

    private static SafetyResult scan(String text, String tag) {
        if (text == null) {
            return SafetyResult.pass(tag + ":empty");
        }
        for (String w : BLOCK) {
            if (text.contains(w)) {
                return SafetyResult.reject(tag + ":命中违禁词 " + w);
            }
        }
        return SafetyResult.pass(tag + ":ok");
    }
}
