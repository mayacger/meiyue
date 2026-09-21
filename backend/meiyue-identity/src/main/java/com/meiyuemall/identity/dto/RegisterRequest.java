package com.meiyuemall.identity.dto;

import com.meiyuemall.identity.domain.RoleCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * 注册请求。
 *
 * @param username    登录名（3~64）
 * @param password    明文密码（6~64，仅传输；入库 BCrypt）
 * @param displayName 展示名
 * @param phone       手机号，可空
 * @param role        初始角色：仅允许 BUYER 或 SELLER_OWNER（申请入驻前先注册为店主意向）；
 *                    PLATFORM_ADMIN 禁止自助注册，由种子账号提供
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Size(min = 6, max = 64) String password,
        @NotBlank @Size(max = 128) String displayName,
        @Size(max = 32) String phone,
        RoleCode role
) {
    public RoleCode resolvedRole() {
        if (role == null) {
            return RoleCode.BUYER;
        }
        return role;
    }
}
