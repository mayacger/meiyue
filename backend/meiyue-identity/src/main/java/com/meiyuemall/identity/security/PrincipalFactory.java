package com.meiyuemall.identity.security;

import com.meiyuemall.common.tenant.ActorType;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.identity.domain.RoleCode;
import com.meiyuemall.identity.domain.UserAccount;
import com.meiyuemall.identity.domain.UserStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 将 {@link UserAccount} 组装为 {@link MeiyuePrincipal}（含租户归属）。
 */
@Component
public class PrincipalFactory {

    private final ObjectProvider<SellerTenantLookup> sellerTenantLookup;

    public PrincipalFactory(ObjectProvider<SellerTenantLookup> sellerTenantLookup) {
        this.sellerTenantLookup = sellerTenantLookup;
    }

    public MeiyuePrincipal fromUser(UserAccount user) {
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        Long tenantId = null;
        Long storeId = null;
        SellerTenantLookup lookup = sellerTenantLookup.getIfAvailable();
        if (lookup != null) {
            var binding = lookup.findByUserId(user.getId());
            if (binding.isPresent()) {
                tenantId = binding.get().tenantId();
                storeId = binding.get().storeId();
            }
        }

        ActorType actorType = resolveActorType(user.getRoles());
        boolean enabled = user.getStatus() == UserStatus.ENABLED;
        return new MeiyuePrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                roles,
                tenantId,
                storeId,
                actorType,
                enabled
        );
    }

    /**
     * 参与者类型优先级：平台 &gt; 商家 &gt; 买家 &gt; 匿名。
     */
    public static ActorType resolveActorType(Set<RoleCode> roles) {
        if (roles == null || roles.isEmpty()) {
            return ActorType.ANONYMOUS;
        }
        if (roles.contains(RoleCode.PLATFORM_ADMIN)) {
            return ActorType.PLATFORM;
        }
        if (roles.contains(RoleCode.SELLER_OWNER) || roles.contains(RoleCode.SELLER_STAFF)) {
            return ActorType.SELLER;
        }
        if (roles.contains(RoleCode.BUYER)) {
            return ActorType.BUYER;
        }
        return ActorType.ANONYMOUS;
    }
}
