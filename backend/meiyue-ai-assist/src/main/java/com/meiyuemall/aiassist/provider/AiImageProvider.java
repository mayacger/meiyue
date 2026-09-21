package com.meiyuemall.aiassist.provider;

/**
 * AI 出图 Provider 端口（可插拔）。
 */
public interface AiImageProvider {

    /** 配置名：MOCK / OPENAI_COMPAT */
    String name();

    /**
     * 生成商品图。
     *
     * @param prompt 提示词
     * @return 可访问 URL（MOCK 为 mock:// 占位）
     */
    GenerateResult generate(String prompt);

    record GenerateResult(boolean success, String url, String source, String errorMessage) {
        public static GenerateResult ok(String url, String source) {
            return new GenerateResult(true, url, source, null);
        }

        public static GenerateResult fail(String source, String errorMessage) {
            return new GenerateResult(false, null, source, errorMessage);
        }
    }
}
