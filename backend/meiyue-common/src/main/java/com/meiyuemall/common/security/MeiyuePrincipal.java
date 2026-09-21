package com.meiyuemall.common.security;

import com.meiyuemall.common.tenant.ActorType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证主体：写入 SecurityContext，供控制器与方法级鉴权使用。
 * <p>
 * 字段说明：
 * <ul>
 *   <li>{@code userId} — 用户主键</li>
 *   <li>{@code username} — 登录名</li>
 *   <li>{@code passwordHash} — 仅 UserDetailsService 加载时使用；JWT 过滤器构造时可为空串</li>
 *   <li>{@code roles} — 角色码集合（不含 ROLE_ 前缀），如 BUYER</li>
 *   <li>{@code tenantId}/{@code storeId} — 商家成员归属；买家/平台为 null</li>
 *   <li>{@code actorType} — 推导出的参与者类型</li>
 * </ul>
 * </p>
 */
public class MeiyuePrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String passwordHash;
    private final Set<String> roles;
    private final Long tenantId;
    private final Long storeId;
    private final ActorType actorType;
    private final boolean enabled;

    public MeiyuePrincipal(
            Long userId,
            String username,
            String passwordHash,
            Set<String> roles,
            Long tenantId,
            Long storeId,
            ActorType actorType,
            boolean enabled
    ) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash == null ? "" : passwordHash;
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
        this.tenantId = tenantId;
        this.storeId = storeId;
        this.actorType = actorType == null ? ActorType.ANONYMOUS : actorType;
        this.enabled = enabled;
    }

    public Long getUserId() {
        return userId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public ActorType getActorType() {
        return actorType;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
