package com.meiyuemall.payment.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.payment.channel.ChannelNotifyResult;
import com.meiyuemall.payment.channel.ChannelQueryResult;
import com.meiyuemall.payment.channel.PaymentChannelClient;
import com.meiyuemall.payment.channel.PaymentChannelRegistry;
import com.meiyuemall.payment.config.PaymentProperties;
import com.meiyuemall.payment.domain.Payment;
import com.meiyuemall.payment.domain.PaymentChannel;
import com.meiyuemall.payment.domain.PaymentNotifyLog;
import com.meiyuemall.payment.domain.PaymentQueryLog;
import com.meiyuemall.payment.domain.PaymentStatus;
import com.meiyuemall.payment.dto.PaymentResponse;
import com.meiyuemall.payment.repo.PaymentNotifyLogRepository;
import com.meiyuemall.payment.repo.PaymentQueryLogRepository;
import com.meiyuemall.payment.repo.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * 支付编排服务（I4）：建单、MOCK 支付、回调验签幂等、主动查单。
 * <p>金额以后端支付单为准；通道密钥不入库。</p>
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentNotifyLogRepository notifyLogRepository;
    private final PaymentQueryLogRepository queryLogRepository;
    private final PaymentChannelRegistry channelRegistry;
    private final PaymentProperties paymentProperties;
    private final ObjectProvider<PaymentSuccessHandler> successHandler;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentNotifyLogRepository notifyLogRepository,
            PaymentQueryLogRepository queryLogRepository,
            PaymentChannelRegistry channelRegistry,
            PaymentProperties paymentProperties,
            ObjectProvider<PaymentSuccessHandler> successHandler
    ) {
        this.paymentRepository = paymentRepository;
        this.notifyLogRepository = notifyLogRepository;
        this.queryLogRepository = queryLogRepository;
        this.channelRegistry = channelRegistry;
        this.paymentProperties = paymentProperties;
        this.successHandler = successHandler;
    }

    /**
     * 创建待支付单；默认通道来自配置（本地 MOCK）。
     */
    @Transactional
    public PaymentResponse createPending(Long orderId, long amountCents, PaymentChannel channel) {
        PaymentChannel resolved = resolveChannel(channel);
        if (resolved == PaymentChannel.MOCK && !paymentProperties.mockEnabled()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "MOCK 支付已关闭");
        }
        String idempotentKey = "order:" + orderId;
        return paymentRepository.findByIdempotentKey(idempotentKey)
                .map(this::toResponse)
                .orElseGet(() -> {
                    Payment payment = new Payment();
                    payment.setPaymentNo(genNo("P"));
                    payment.setOrderId(orderId);
                    payment.setChannel(resolved);
                    payment.setStatus(PaymentStatus.PENDING);
                    payment.setAmountCents(amountCents);
                    payment.setIdempotentKey(idempotentKey);
                    paymentRepository.save(payment);
                    // 尝试通道预下单（MOCK 返回摘要；微信/支付宝骨架会抛错——仅当显式选中时）
                    if (resolved == PaymentChannel.MOCK) {
                        channelRegistry.get(resolved).createPrepay(payment);
                    }
                    return toResponse(payment);
                });
    }

    /** 本地模拟支付成功（仅 MOCK） */
    @Transactional
    public PaymentResponse mockPaySuccess(String paymentNo) {
        Payment payment = requirePayment(paymentNo);
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return toResponse(payment);
        }
        if (payment.getChannel() != PaymentChannel.MOCK) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非 MOCK 通道请走异步回调或查单");
        }
        if (!paymentProperties.mockEnabled()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "MOCK 支付已关闭");
        }
        markSuccess(payment, "mock_" + payment.getPaymentNo());
        return toResponse(payment);
    }

    /**
     * 处理通道异步通知：验签 → 幂等（channel+tradeNo）→ 金额校验 → 置成功。
     *
     * @return 给通道的应答文案（如 success / fail）
     */
    @Transactional
    public String handleNotify(PaymentChannel channel, String rawBody, Map<String, String> headers) {
        PaymentChannelClient client = channelRegistry.get(channel);
        ChannelNotifyResult parsed = client.parseAndVerifyNotify(rawBody, headers);

        String tradeNo = parsed.channelTradeNo() == null || parsed.channelTradeNo().isBlank()
                ? "unknown_" + UUID.randomUUID()
                : parsed.channelTradeNo();

        // 幂等：唯一约束冲突视为重复通知
        if (notifyLogRepository.findByChannelAndChannelTradeNo(channel.name(), tradeNo).isPresent()) {
            return "success";
        }

        PaymentNotifyLog notifyLog = new PaymentNotifyLog();
        notifyLog.setChannel(channel.name());
        notifyLog.setChannelTradeNo(tradeNo);
        notifyLog.setPaymentNo(parsed.paymentNo());
        notifyLog.setPayloadHash(sha256(rawBody));
        notifyLog.setRawBodyPreview(parsed.rawPreview());
        notifyLog.setVerifyResult(parsed.verified() ? (channel == PaymentChannel.MOCK ? "SKIP_MOCK" : "PASS") : "FAIL");

        if (!parsed.verified()) {
            notifyLog.setProcessResult("ERROR");
            notifyLog.setErrorMessage("验签失败");
            saveNotifyQuietly(notifyLog);
            return "fail";
        }
        if (!parsed.success()) {
            notifyLog.setProcessResult("IGNORED");
            saveNotifyQuietly(notifyLog);
            return "success";
        }

        try {
            Payment payment = requirePayment(parsed.paymentNo());
            notifyLog.setOrderId(payment.getOrderId());
            // 金额以后端为准
            if (parsed.amountCents() != null && parsed.amountCents() != payment.getAmountCents()) {
                notifyLog.setProcessResult("ERROR");
                notifyLog.setErrorMessage("金额不一致 local=" + payment.getAmountCents() + " channel=" + parsed.amountCents());
                saveNotifyQuietly(notifyLog);
                return "fail";
            }
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                notifyLog.setProcessResult("DUPLICATE");
                saveNotifyQuietly(notifyLog);
                return "success";
            }
            markSuccess(payment, tradeNo);
            payment.setNotifyCount(payment.getNotifyCount() + 1);
            notifyLog.setProcessResult("SUCCESS");
            saveNotifyQuietly(notifyLog);
            return "success";
        } catch (RuntimeException ex) {
            notifyLog.setProcessResult("ERROR");
            notifyLog.setErrorMessage(ex.getMessage());
            saveNotifyQuietly(notifyLog);
            throw ex;
        }
    }

    /** 主动查单补偿：仅处理 PENDING */
    @Transactional
    public PaymentResponse queryAndSync(String paymentNo) {
        Payment payment = requirePayment(paymentNo);
        PaymentChannelClient client = channelRegistry.get(payment.getChannel());
        ChannelQueryResult result = client.queryOrder(payment);

        PaymentQueryLog qlog = new PaymentQueryLog();
        qlog.setPaymentNo(paymentNo);
        qlog.setChannel(payment.getChannel().name());
        qlog.setChannelTradeNo(result.channelTradeNo());
        qlog.setRawPreview(result.rawPreview());
        if (!result.found()) {
            qlog.setQueryResult("NOT_FOUND");
        } else if (result.paid()) {
            qlog.setQueryResult("SUCCESS");
            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                String tradeNo = result.channelTradeNo() == null ? "query_" + paymentNo : result.channelTradeNo();
                markSuccess(payment, tradeNo);
            }
        } else {
            qlog.setQueryResult("PENDING");
        }
        payment.setLastQueryAt(Instant.now());
        queryLogRepository.save(qlog);
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByPaymentNo(String paymentNo) {
        return toResponse(requirePayment(paymentNo));
    }

    @Transactional(readOnly = true)
    public PaymentResponse findLatestByOrderId(Long orderId) {
        return paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .map(this::toResponse)
                .orElse(null);
    }

    private void markSuccess(Payment payment, String channelTradeNo) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setChannelTradeNo(channelTradeNo);
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);
        PaymentSuccessHandler handler = successHandler.getIfAvailable();
        if (handler != null) {
            handler.onPaymentSuccess(payment.getOrderId(), payment.getPaymentNo(), channelTradeNo);
        } else {
            log.warn("未注册 PaymentSuccessHandler，订单状态需人工补偿 orderId={}", payment.getOrderId());
        }
    }

    private PaymentChannel resolveChannel(PaymentChannel requested) {
        if (requested != null) {
            return requested;
        }
        String def = paymentProperties.defaultChannel();
        if (def == null || def.isBlank()) {
            return PaymentChannel.MOCK;
        }
        return PaymentChannel.valueOf(def.trim().toUpperCase());
    }

    private Payment requirePayment(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "支付单不存在"));
    }

    private void saveNotifyQuietly(PaymentNotifyLog notifyLog) {
        try {
            notifyLogRepository.save(notifyLog);
        } catch (DataIntegrityViolationException dup) {
            log.info("回调幂等命中 channel={} tradeNo={}", notifyLog, dup.getMessage());
        }
    }

    private static String genNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    private static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest((raw == null ? "" : raw).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            return null;
        }
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
