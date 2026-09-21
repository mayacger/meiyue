package com.meiyuemall.aiassist.provider;

/**
 * AI 推广视频 Provider（非直播）。真实模型后置，默认 MOCK。
 */
public interface AiVideoProvider {

    String name();

    VideoResult generate(String prompt);

    record VideoResult(boolean success, String source, String url, String errorMessage) {}
}
