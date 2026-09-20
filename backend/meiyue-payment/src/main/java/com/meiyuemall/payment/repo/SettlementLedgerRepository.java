package com.meiyuemall.payment.repo;

import com.meiyuemall.payment.domain.SettlementLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementLedgerRepository extends JpaRepository<SettlementLedger, Long> {
    List<SettlementLedger> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, String status);

    List<SettlementLedger> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<SettlementLedger> findByTenantIdAndPeriodKeyOrderByCreatedAtDesc(Long tenantId, String periodKey);

    boolean existsByOrderIdAndEntryType(Long orderId, String entryType);

    @Query("""
            select s.periodKey, s.status, coalesce(sum(s.amountCents), 0), count(s)
            from SettlementLedger s
            where s.tenantId = :tenantId
            group by s.periodKey, s.status
            order by s.periodKey desc
            """)
    List<Object[]> summarizeByPeriod(@Param("tenantId") Long tenantId);
}
