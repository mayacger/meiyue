package com.meiyuemall.payment.job;

import com.meiyuemall.payment.domain.Payment;
import com.meiyuemall.payment.domain.PaymentChannel;
import com.meiyuemall.payment.domain.PaymentStatus;
import com.meiyuemall.payment.repo.PaymentRepository;
import com.meiyuemall.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 支付查单补偿任务：对创建超过 2 分钟仍 PENDING 的单据主动查单。
 */
@Component
public class PaymentQueryJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentQueryJob.class);

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public PaymentQueryJob(PaymentRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelayString = "120000")
    public void sweepPending() {
        Instant threshold = Instant.now().minus(2, ChronoUnit.MINUTES);
        for (PaymentChannel channel : PaymentChannel.values()) {
            List<Payment> list = paymentRepository.findByStatusAndChannelAndCreatedAtBefore(
                    PaymentStatus.PENDING, channel, threshold);
            for (Payment payment : list) {
                try {
                    paymentService.queryAndSync(payment.getPaymentNo());
                } catch (Exception ex) {
                    log.warn("查单失败 paymentNo={}: {}", payment.getPaymentNo(), ex.getMessage());
                }
            }
            if (!list.isEmpty()) {
                log.info("查单补偿通道={} 处理{}笔", channel, list.size());
            }
        }
    }
}
