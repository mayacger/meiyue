package com.meiyuemall.common.tenant;

/**
 * 参与者类型（注入 TenantContext 后用于鉴权与审计）。
 * <ul>
 *   <li>{@link #BUYER} — 平台级买家，可跨店下单，通常无商家 tenantId</li>
 *   <li>{@link #SELLER} — 商家店主/员工，读写必须绑定本店 tenantId</li>
 *   <li>{@link #PLATFORM} — 平台管理员，可跨租户治理，不伪装成商家</li>
 *   <li>{@link #ANONYMOUS} — 未登录或脚手架阶段占位</li>
 * </ul>
 */
public enum ActorType {

    /** 买家：平台级账号 */
    BUYER,

    /** 商家侧主体（店主或员工） */
    SELLER,

    /** 平台运营/管理员 */
    PLATFORM,

    /** 匿名或尚未解析身份 */
    ANONYMOUS
}
