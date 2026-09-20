package com.meiyuemall.payment.repo;

import com.meiyuemall.payment.domain.Payment;
import com.meiyuemall.payment.domain.PaymentChannel;
import com.meiyuemall.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentNo(String paymentNo);
    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);
    Optional<Payment> findByIdempotentKey(String idempotentKey);
    List<Payment> findByStatusAndChannelAndCreatedAtBefore(PaymentStatus status, PaymentChannel channel, Instant createdBefore);
    List<Payment> findByStatusAndPaidAtBetween(PaymentStatus status, Instant from, Instant to);
}
