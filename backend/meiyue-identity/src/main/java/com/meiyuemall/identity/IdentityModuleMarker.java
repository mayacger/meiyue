package com.meiyuemall.identity;

/**
 * IAM 模块标记（空壳）。
 * <p>
 * I1 将在此包下补充：用户实体、密码哈希（BCrypt）、JWT/Session、RBAC。
 * </p>
 */
public final class IdentityModuleMarker {

    private IdentityModuleMarker() {
    }

    /** 模块工程标识，便于启动日志确认 classpath */
    public static final String MODULE_ID = "meiyue-identity";
}
