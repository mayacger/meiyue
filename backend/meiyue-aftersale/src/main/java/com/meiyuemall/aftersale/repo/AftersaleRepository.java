package com.meiyuemall.aftersale.repo;

import com.meiyuemall.aftersale.domain.Aftersale;
import com.meiyuemall.aftersale.domain.AftersaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /** I36：商家区间已关闭售后退款合计（分） */
    @Query("""
            select coalesce(sum(a.refundCents), 0) from Aftersale a
            where a.tenantId = :tenantId
              and a.status = com.meiyuemall.aftersale.domain.AftersaleStatus.CLOSED
              and a.closedAt is not null
              and a.closedAt >= :from and a.closedAt < :to
            """)
    long sumClosedRefundCentsForSeller(
            @Param("tenantId") Long tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    /** I36：平台区间已关闭售后退款合计（分） */
    @Query("""
            select coalesce(sum(a.refundCents), 0) from Aftersale a
            where a.status = com.meiyuemall.aftersale.domain.AftersaleStatus.CLOSED
              and a.closedAt is not null
              and a.closedAt >= :from and a.closedAt < :to
            """)
    long sumClosedRefundCentsBetween(@Param("from") Instant from, @Param("to") Instant to);
}
