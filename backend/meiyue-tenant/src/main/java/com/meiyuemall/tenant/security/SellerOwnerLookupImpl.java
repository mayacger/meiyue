package com.meiyuemall.tenant.security;

import com.meiyuemall.common.tenant.SellerOwnerLookup;
import com.meiyuemall.tenant.domain.MemberRole;
import com.meiyuemall.tenant.repo.SellerMemberRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** 按租户查 OWNER userId */
@Component
public class SellerOwnerLookupImpl implements SellerOwnerLookup {

    private final SellerMemberRepository sellerMemberRepository;

    public SellerOwnerLookupImpl(SellerMemberRepository sellerMemberRepository) {
        this.sellerMemberRepository = sellerMemberRepository;
    }

    @Override
    public Optional<Long> findOwnerUserId(Long tenantId) {
        if (tenantId == null) {
            return Optional.empty();
        }
        return sellerMemberRepository.findFirstByTenantIdAndMemberRole(tenantId, MemberRole.OWNER)
                .map(m -> m.getUserId());
    }
}
