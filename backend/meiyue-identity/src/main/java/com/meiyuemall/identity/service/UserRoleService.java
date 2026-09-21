package com.meiyuemall.identity.service;

import com.meiyuemall.identity.domain.RoleCode;
import com.meiyuemall.identity.domain.UserAccount;
import com.meiyuemall.identity.repo.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 供租户模块在审核通过时授予商家角色，避免直接操作仓储细节散落。
 */
@Service
public class UserRoleService {

    private final UserAccountRepository userAccountRepository;

    public UserRoleService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public void grantRole(Long userId, RoleCode role) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
        user.getRoles().add(role);
        userAccountRepository.save(user);
    }

    /** I26：移除角色（如解除店员） */
    @Transactional
    public void revokeRole(Long userId, RoleCode role) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
        user.getRoles().remove(role);
        userAccountRepository.save(user);
    }
}
