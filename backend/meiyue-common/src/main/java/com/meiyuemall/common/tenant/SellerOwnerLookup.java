package com.meiyuemall.common.tenant;

import java.util.Optional;

/**
 * 按租户查找商家 OWNER 用户 ID（通知投递用）。
 */
public interface SellerOwnerLookup {

    /** @return OWNER 的 userId；无成员则 empty */
    Optional<Long> findOwnerUserId(Long tenantId);
}
