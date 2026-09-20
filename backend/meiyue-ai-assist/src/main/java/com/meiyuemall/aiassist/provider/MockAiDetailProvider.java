package com.meiyuemall.aiassist.provider;

import org.springframework.stereotype.Component;

/**
 * 本地 MOCK 详情：根据标题拼装简易 HTML 卖点。
 */
@Component
public class MockAiDetailProvider implements AiDetailProvider {

    @Override
    public String name() {
        return "MOCK";
    }

    @Override
    public DetailResult generate(String title, String hints) {
        String safeTitle = title == null || title.isBlank() ? "精选商品" : title.trim();
        String hintLine = hints == null || hints.isBlank() ? "品质精选，产地直发" : hints.trim();
        String html = "<section><h2>" + escape(safeTitle) + "</h2>"
                + "<p>" + escape(hintLine) + "</p>"
                + "<ul><li>新鲜直达</li><li>售后无忧</li><li>美月商城严选</li></ul></section>";
        return DetailResult.ok(safeTitle, html, "AI_MOCK");
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
