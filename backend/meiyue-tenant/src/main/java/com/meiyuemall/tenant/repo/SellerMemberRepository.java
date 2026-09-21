package com.meiyuemall.tenant.repo;

import com.meiyuemall.tenant.domain.MemberRole;
import com.meiyuemall.tenant.domain.SellerMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerMemberRepository extends JpaRepository<SellerMember, Long> {

    Optional<SellerMember> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    /** I11：租户 OWNER，用于站内通知投递 */
    Optional<SellerMember> findFirstByTenantIdAndMemberRole(Long tenantId, MemberRole memberRole);

    /** I26：本店成员列表 */
    List<SellerMember> findByTenantIdOrderByCreatedAtAsc(Long tenantId);

    Optional<SellerMember> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndUserId(Long tenantId, Long userId);
}
