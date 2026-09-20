package com.meiyuemall.identity.config;

import com.meiyuemall.identity.domain.RoleCode;
import com.meiyuemall.identity.domain.UserAccount;
import com.meiyuemall.identity.domain.UserStatus;
import com.meiyuemall.identity.repo.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

/**
 * 种子数据：确保存在默认平台管理员 {@code admin / admin123}（仅本地开发）。
 */
@Component
public class PlatformAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PlatformAdminSeeder.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public PlatformAdminSeeder(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userAccountRepository.existsByUsername("admin")) {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setDisplayName("平台管理员");
        admin.setStatus(UserStatus.ENABLED);
        admin.setRoles(EnumSet.of(RoleCode.PLATFORM_ADMIN));
        userAccountRepository.save(admin);
        log.info("已初始化平台管理员账号 admin（默认密码 admin123，生产请立即更换）");
    }
}
