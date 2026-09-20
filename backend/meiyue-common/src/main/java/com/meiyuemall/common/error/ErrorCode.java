package com.meiyuemall.common.error;

/**
 * 平台统一错误码（脚手架占位）。
 * <p>
 * 约定：业务码字符串稳定，便于前端与监控映射；HTTP 状态由全局异常处理映射（I1 补齐）。
 * </p>
 *
 * @param code    稳定业务码，例如 {@code TENANT_REQUIRED}
 * @param message 默认中文说明（可被 i18n 覆盖）
 */
public record ErrorCode(String code, String message) {

    /** 通用未认证 */
    public static final ErrorCode UNAUTHORIZED = new ErrorCode("UNAUTHORIZED", "未登录或凭证无效");

    /** 通用无权限 */
    public static final ErrorCode FORBIDDEN = new ErrorCode("FORBIDDEN", "无权限访问该资源");

    /** 商家侧操作缺少租户上下文 */
    public static final ErrorCode TENANT_REQUIRED = new ErrorCode("TENANT_REQUIRED", "缺少租户上下文");

    /** 疑似串租或资源不属于当前租户 */
    public static final ErrorCode TENANT_MISMATCH = new ErrorCode("TENANT_MISMATCH", "租户不匹配");
}
