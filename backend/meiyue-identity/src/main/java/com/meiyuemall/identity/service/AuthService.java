package com.meiyuemall.identity.service;

import com.meiyuemall.common.audit.Audited;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.identity.config.JwtProperties;
import com.meiyuemall.identity.domain.RoleCode;
import com.meiyuemall.identity.domain.UserAccount;
import com.meiyuemall.identity.domain.UserStatus;
import com.meiyuemall.identity.dto.AuthResponse;
import com.meiyuemall.identity.dto.ChangePasswordRequest;
import com.meiyuemall.identity.dto.LoginRequest;
import com.meiyuemall.identity.dto.RegisterRequest;
import com.meiyuemall.identity.dto.UpdateProfileRequest;
import com.meiyuemall.identity.dto.UserProfileResponse;
import com.meiyuemall.identity.repo.UserAccountRepository;
import com.meiyuemall.identity.security.JwtService;
import com.meiyuemall.identity.security.PrincipalFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 注册 / 登录 / 当前用户。
 */
@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PrincipalFactory principalFactory;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            PrincipalFactory principalFactory
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.principalFactory = principalFactory;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        RoleCode role = request.resolvedRole();
        // 禁止自助注册平台管理员
        if (role == RoleCode.PLATFORM_ADMIN || role == RoleCode.SELLER_STAFF) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不允许自助注册该角色");
        }
        if (userAccountRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }
        UserAccount user = new UserAccount();
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        user.setPhone(request.phone());
        user.setStatus(UserStatus.ENABLED);
        // 自助注册一律先给 BUYER；SELLER_OWNER 仅在入驻审核通过后由 OnboardingService 授予
        // request.role=SELLER_OWNER 仅表示前端「商家注册」意向，不提前提权
        user.setRoles(EnumSet.of(RoleCode.BUYER));
        userAccountRepository.save(user);
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (user.getStatus() != UserStatus.ENABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已禁用");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse me() {
        MeiyuePrincipal principal = SecurityUtils.requirePrincipal();
        UserAccount user = userAccountRepository.findById(principal.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return toProfile(user, principalFactory.fromUser(user));
    }

    /**
     * I24：更新展示名 / 手机号（username 不可改）。
     */
    @Transactional
    @Audited(action = "PROFILE_UPDATE", resourceType = "UserAccount")
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        MeiyuePrincipal principal = SecurityUtils.requirePrincipal();
        UserAccount user = userAccountRepository.findById(principal.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        if (request.displayName() != null && !request.displayName().isBlank()) {
            user.setDisplayName(request.displayName().trim());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone().isBlank() ? null : request.phone().trim());
        }
        return toProfile(user, principalFactory.fromUser(user));
    }

    /**
     * I24：修改密码（校验旧密码；新密码入库 BCrypt；不写明文到审计）。
     */
    @Transactional
    @Audited(action = "PASSWORD_CHANGE", resourceType = "UserAccount")
    public void changePassword(ChangePasswordRequest request) {
        MeiyuePrincipal principal = SecurityUtils.requirePrincipal();
        UserAccount user = userAccountRepository.findById(principal.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "当前密码不正确");
        }
        if (request.oldPassword().equals(request.newPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "新密码不能与当前密码相同");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private AuthResponse toAuthResponse(UserAccount user) {
        MeiyuePrincipal principal = principalFactory.fromUser(user);
        String token = jwtService.issueToken(user.getId(), user.getUsername(), user.getRoles());
        return new AuthResponse(
                token,
                "Bearer",
                jwtProperties.expireSeconds(),
                toProfile(user, principal)
        );
    }

    private static UserProfileResponse toProfile(UserAccount user, MeiyuePrincipal principal) {
        Set<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getPhone(),
                roles,
                principal.getTenantId(),
                principal.getStoreId(),
                principal.getActorType().name()
        );
    }
}
