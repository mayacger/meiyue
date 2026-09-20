package com.meiyuemall.payment.repo;

import com.meiyuemall.payment.domain.SettlementLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SettlementLedgerRepository extends JpaRepository<SettlementLedger, Long> {
    List<SettlementLedger> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, String status);
    boolean existsByOrderIdAndEntryType(Long orderId, String entryType);
}
