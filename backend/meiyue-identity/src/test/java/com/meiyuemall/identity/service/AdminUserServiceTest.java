package com.meiyuemall.identity.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.identity.domain.RoleCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I18：用户目录角色过滤解析单测（无 Spring 上下文）。
 */
class AdminUserServiceTest {

    @Test
    void blankRoleFilterMeansNoFilter() {
        assertNull(AdminUserService.parseRoleFilter(null));
        assertNull(AdminUserService.parseRoleFilter(""));
        assertNull(AdminUserService.parseRoleFilter("  "));
    }

    @Test
    void parsesKnownRole() {
        assertEquals(RoleCode.BUYER, AdminUserService.parseRoleFilter("BUYER"));
        assertEquals(RoleCode.SELLER_OWNER, AdminUserService.parseRoleFilter(" SELLER_OWNER "));
    }

    @Test
    void rejectsUnknownRole() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> AdminUserService.parseRoleFilter("NOT_A_ROLE"));
        assertTrue(ex.getMessage().contains("无效角色"));
    }
}
