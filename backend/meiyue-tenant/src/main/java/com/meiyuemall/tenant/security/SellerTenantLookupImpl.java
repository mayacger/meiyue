package com.meiyuemall.tenant.security;

import com.meiyuemall.identity.security.SellerTenantLookup;
import com.meiyuemall.tenant.domain.SellerMember;
import com.meiyuemall.tenant.repo.SellerMemberRepository;
import com.meiyuemall.tenant.repo.StoreRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 实现 identity 端口：按用户查租户/店铺归属。
 */
@Component
public class SellerTenantLookupImpl implements SellerTenantLookup {

    private final SellerMemberRepository sellerMemberRepository;
    private final StoreRepository storeRepository;

    public SellerTenantLookupImpl(
            SellerMemberRepository sellerMemberRepository,
            StoreRepository storeRepository
    ) {
        this.sellerMemberRepository = sellerMemberRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public Optional<Binding> findByUserId(Long userId) {
        Optional<SellerMember> member = sellerMemberRepository.findByUserId(userId);
        if (member.isEmpty()) {
            return Optional.empty();
        }
        Long tenantId = member.get().getTenantId();
        Long storeId = storeRepository.findByTenantId(tenantId).map(s -> s.getId()).orElse(null);
        return Optional.of(new Binding(tenantId, storeId));
    }
}
