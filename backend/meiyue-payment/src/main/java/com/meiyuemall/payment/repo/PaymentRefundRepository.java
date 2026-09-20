package com.meiyuemall.payment.repo;

import com.meiyuemall.payment.domain.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
    Optional<PaymentRefund> findByAftersaleId(Long aftersaleId);
}
