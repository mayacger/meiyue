package com.meiyuemall.identity.security;

/**
 * 商家租户归属查询端口（由 meiyue-tenant 实现）。
 * <p>
 * 避免 identity → tenant 循环依赖：JWT 组装主体时通过此端口取 tenantId/storeId。
 * </p>
 */
public interface SellerTenantLookup {

    /**
     * @param userId 用户 ID
     * @return 若该用户是某店成员则返回租户与店铺；否则 empty
     */
    java.util.Optional<Binding> findByUserId(Long userId);

    /**
     * @param tenantId 租户 ID
     * @param storeId  店铺 ID（MVP 与租户 1:1）
     */
    record Binding(Long tenantId, Long storeId) {
    }
}
