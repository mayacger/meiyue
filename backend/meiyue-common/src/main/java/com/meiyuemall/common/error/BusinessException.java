package com.meiyuemall.common.error;

/**
 * 业务异常：由全局处理器映射为 HTTP 状态与 {@link com.meiyuemall.common.web.ApiResponse}。
 *
 * @param errorCode 稳定业务码
 * @param detail    可选补充说明（可空，默认用 errorCode.message）
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail == null || detail.isBlank() ? errorCode.message() : detail);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
