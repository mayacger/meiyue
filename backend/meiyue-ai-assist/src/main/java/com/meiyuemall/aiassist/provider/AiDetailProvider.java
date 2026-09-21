package com.meiyuemall.aiassist.provider;

/**
 * AI 详情文案 Provider 端口。
 */
public interface AiDetailProvider {

    String name();

    DetailResult generate(String title, String hints);

    record DetailResult(boolean success, String titleSuggest, String detailHtml, String source, String errorMessage) {
        public static DetailResult ok(String titleSuggest, String detailHtml, String source) {
            return new DetailResult(true, titleSuggest, detailHtml, source, null);
        }

        public static DetailResult fail(String source, String errorMessage) {
            return new DetailResult(false, null, null, source, errorMessage);
        }
    }
}
