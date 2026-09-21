package com.meiyuemall.common.error;

/**
 * 平台统一错误码（I1 扩展）。
 *
 * @param code    稳定业务码
 * @param message 默认中文说明
 */
public record ErrorCode(String code, String message) {

    public static final ErrorCode UNAUTHORIZED = new ErrorCode("UNAUTHORIZED", "未登录或凭证无效");
    public static final ErrorCode FORBIDDEN = new ErrorCode("FORBIDDEN", "无权限访问该资源");
    public static final ErrorCode TENANT_REQUIRED = new ErrorCode("TENANT_REQUIRED", "缺少租户上下文");
    public static final ErrorCode TENANT_MISMATCH = new ErrorCode("TENANT_MISMATCH", "租户不匹配");
    public static final ErrorCode BAD_REQUEST = new ErrorCode("BAD_REQUEST", "请求参数不合法");
    public static final ErrorCode NOT_FOUND = new ErrorCode("NOT_FOUND", "资源不存在");
    public static final ErrorCode CONFLICT = new ErrorCode("CONFLICT", "资源冲突");
    public static final ErrorCode USER_EXISTS = new ErrorCode("USER_EXISTS", "用户名已存在");
    public static final ErrorCode INVALID_CREDENTIALS = new ErrorCode("INVALID_CREDENTIALS", "用户名或密码错误");
    public static final ErrorCode APPLICATION_EXISTS = new ErrorCode("APPLICATION_EXISTS", "已有进行中的入驻申请或店铺");
    public static final ErrorCode APPLICATION_NOT_PENDING = new ErrorCode("APPLICATION_NOT_PENDING", "申请不在待审核状态");
    /** I7：接口限流 */
    public static final ErrorCode RATE_LIMITED = new ErrorCode("RATE_LIMITED", "请求过于频繁，请稍后再试");
    /** I8：AI 失败，应降级人工上传 */
    public static final ErrorCode AI_DEGRADED = new ErrorCode("AI_DEGRADED", "AI 生成失败，请改用人工上传");
    /** I8：素材未过审不可挂接 */
    public static final ErrorCode MEDIA_NOT_APPROVED = new ErrorCode("MEDIA_NOT_APPROVED", "素材未通过内容安全审核");
}
