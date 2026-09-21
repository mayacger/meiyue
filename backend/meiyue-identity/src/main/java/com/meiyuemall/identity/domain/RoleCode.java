package com.meiyuemall.identity.domain;

/**
 * 用户角色码（入库与 JWT claims 使用，不含 ROLE_ 前缀）。
 */
public enum RoleCode {
    /** 买家 */
    BUYER,
    /** 商家店主 */
    SELLER_OWNER,
    /** 商家员工 */
    SELLER_STAFF,
    /** 平台管理员 */
    PLATFORM_ADMIN
}
