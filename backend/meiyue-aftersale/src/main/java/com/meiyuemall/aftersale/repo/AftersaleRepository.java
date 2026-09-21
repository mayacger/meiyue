package com.meiyuemall.aftersale.repo;

import com.meiyuemall.aftersale.domain.Aftersale;
import com.meiyuemall.aftersale.domain.AftersaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AftersaleRepository extends JpaRepository<Aftersale, Long> {
    List<Aftersale> findByBuyerUserIdOrderByCreatedAtDesc(Long buyerUserId);
    List<Aftersale> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<Aftersale> findByIdAndTenantId(Long id, Long tenantId);
    Optional<Aftersale> findByIdAndBuyerUserId(Long id, Long buyerUserId);
    List<Aftersale> findByStatusInAndSellerDeadlineAtBefore(List<AftersaleStatus> statuses, Instant deadline);

    /** 待处理售后数（APPLIED / REVIEWING） */
    long countByTenantIdAndStatusIn(Long tenantId, Collection<AftersaleStatus> statuses);
}
