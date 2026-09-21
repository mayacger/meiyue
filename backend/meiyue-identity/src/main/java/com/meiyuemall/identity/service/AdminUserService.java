package com.meiyuemall.identity.service;

import com.meiyuemall.common.audit.Audited;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.identity.domain.RoleCode;
import com.meiyuemall.identity.domain.UserAccount;
import com.meiyuemall.identity.domain.UserStatus;
import com.meiyuemall.identity.dto.AdminUserResponse;
import com.meiyuemall.identity.repo.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 平台用户目录（I18）：列表只读 + 启停。
 * <p>禁止管理员禁用自身，避免锁死运营入口。</p>
 */
@Service
public class AdminUserService {

    private final UserAccountRepository userAccountRepository;

    public AdminUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * 列出用户；可选按角色过滤（如 BUYER / SELLER_OWNER）。
     *
     * @param roleFilter 角色名，可空；非法值抛 BAD_REQUEST
     */
    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers(String roleFilter) {
        RoleCode filter = parseRoleFilter(roleFilter);
        return userAccountRepository.findAll().stream()
                .filter(u -> filter == null || u.getRoles().contains(filter))
                .sorted(Comparator.comparing(UserAccount::getId))
                .map(this::toResponse)
                .toList();
    }

    /**
     * 启停账号。
     *
     * @param userId 目标用户
     * @param status ENABLED / DISABLED
     */
    @Transactional
    @Audited(action = "USER_STATUS", resourceType = "UserAccount")
    public AdminUserResponse changeStatus(Long userId, UserStatus status) {
        Long selfId = SecurityUtils.requirePrincipal().getUserId();
        if (selfId.equals(userId) && status == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能禁用当前登录账号");
        }
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        user.setStatus(status);
        return toResponse(user);
    }

    /** 解析可选角色过滤；空串视为不过滤 */
    static RoleCode parseRoleFilter(String roleFilter) {
        if (roleFilter == null || roleFilter.isBlank()) {
            return null;
        }
        try {
            return RoleCode.valueOf(roleFilter.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效角色过滤: " + roleFilter);
        }
    }

    private AdminUserResponse toResponse(UserAccount u) {
        Set<RoleCode> roles = u.getRoles() == null
                ? EnumSet.noneOf(RoleCode.class)
                : EnumSet.copyOf(u.getRoles());
        return new AdminUserResponse(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getPhone(),
                u.getStatus(),
                roles,
                u.getCreatedAt()
        );
    }
}
