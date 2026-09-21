package com.meiyuemall.payment.repo;

import com.meiyuemall.payment.domain.PaymentQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentQueryLogRepository extends JpaRepository<PaymentQueryLog, Long> {
}
