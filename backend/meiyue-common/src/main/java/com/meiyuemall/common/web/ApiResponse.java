package com.meiyuemall.common.web;

/**
 * 统一 API 响应外壳（脚手架）。
 *
 * @param success 是否成功
 * @param code    业务码；成功时通常为 {@code OK}
 * @param message 人类可读说明
 * @param data    业务载荷
 * @param <T>     载荷类型
 */
public record ApiResponse<T>(boolean success, String code, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", "success", data);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
