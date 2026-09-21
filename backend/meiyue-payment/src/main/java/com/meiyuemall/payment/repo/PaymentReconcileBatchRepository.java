package com.meiyuemall.payment.repo;

import com.meiyuemall.payment.domain.PaymentReconcileBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface PaymentReconcileBatchRepository extends JpaRepository<PaymentReconcileBatch, Long> {
    Optional<PaymentReconcileBatch> findByBizDateAndChannel(LocalDate bizDate, String channel);
}
