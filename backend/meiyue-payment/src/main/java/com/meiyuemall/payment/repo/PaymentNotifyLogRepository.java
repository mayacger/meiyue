package com.meiyuemall.payment.repo;

import com.meiyuemall.payment.domain.PaymentNotifyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentNotifyLogRepository extends JpaRepository<PaymentNotifyLog, Long> {
    Optional<PaymentNotifyLog> findByChannelAndChannelTradeNo(String channel, String channelTradeNo);
}
