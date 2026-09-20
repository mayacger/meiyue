package com.meiyuemall.payment.repo;

import com.meiyuemall.payment.domain.PaymentReconcileDiff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentReconcileDiffRepository extends JpaRepository<PaymentReconcileDiff, Long> {
}
