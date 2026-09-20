package com.meiyuemall.aiassist.safety;

/**
 * 内容安全审核端口（占位可替换真实审核服务）。
 */
public interface ContentSafetyPort {

    SafetyResult reviewText(String text);

    SafetyResult reviewImageUrl(String url, String prompt);

    record SafetyResult(boolean approved, String note) {
        public static SafetyResult pass(String note) {
            return new SafetyResult(true, note);
        }

        public static SafetyResult reject(String note) {
            return new SafetyResult(false, note);
        }
    }
}
