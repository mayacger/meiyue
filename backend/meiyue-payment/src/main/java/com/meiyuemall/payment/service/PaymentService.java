package com.meiyuemall.payment.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.payment.domain.Payment;
import com.meiyuemall.payment.domain.PaymentChannel;
import com.meiyuemall.payment.domain.PaymentStatus;
import com.meiyuemall.payment.dto.PaymentResponse;
import com.meiyuemall.payment.repo.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 支付服务：创建支付单；MOCK 通道立即成功；真实微信/支付宝 I4 对接。
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse createPending(Long orderId, long amountCents, PaymentChannel channel) {
        Payment payment = new Payment();
        payment.setPaymentNo(genNo("P"));
        payment.setOrderId(orderId);
        payment.setChannel(channel == null ? PaymentChannel.MOCK : channel);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmountCents(amountCents);
        paymentRepository.save(payment);
        return toResponse(payment);
    }

    /**
     * 模拟支付成功（幂等：已 SUCCESS 直接返回）。
     * @param onSuccess 支付成功回调（由交易域标记订单已支付）
     */
    @Transactional
    public PaymentResponse mockPaySuccess(String paymentNo, Consumer<Payment> onSuccess) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "支付单不存在"));
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return toResponse(payment);
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付单状态不可支付");
        }
        if (payment.getChannel() != PaymentChannel.MOCK) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非 MOCK 通道请走 I4 真实回调");
        }
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setChannelTradeNo("mock_" + payment.getPaymentNo());
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);
        if (onSuccess != null) {
            onSuccess.accept(payment);
        }
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByPaymentNo(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo)
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "支付单不存在"));
    }

    @Transactional(readOnly = true)
    public PaymentResponse findLatestByOrderId(Long orderId) {
        return paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .map(this::toResponse)
                .orElse(null);
    }

    private static String genNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getPaymentNo(),
                p.getOrderId(),
                p.getChannel().name(),
                p.getStatus().name(),
                p.getAmountCents(),
                p.getChannelTradeNo(),
                p.getPaidAt() == null ? null : p.getPaidAt().toString()
        );
    }
}
