package com.meiyuemall.aftersale.service;

import com.meiyuemall.aftersale.domain.Aftersale;
import com.meiyuemall.aftersale.domain.AftersaleStatus;
import com.meiyuemall.aftersale.domain.AftersaleType;
import com.meiyuemall.aftersale.dto.AftersaleResponse;
import com.meiyuemall.aftersale.dto.ApplyAftersaleRequest;
import com.meiyuemall.aftersale.dto.FillReverseTrackingRequest;
import com.meiyuemall.aftersale.dto.ReviewAftersaleRequest;
import com.meiyuemall.aftersale.repo.AftersaleRepository;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.notify.NotificationPublisher;
import com.meiyuemall.common.redis.DelayTaskPort;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.common.tenant.SellerOwnerLookup;
import com.meiyuemall.logistics.domain.Shipment;
import com.meiyuemall.logistics.service.LogisticsService;
import com.meiyuemall.payment.service.PaymentService;
import com.meiyuemall.payment.service.SettlementLedgerService;
import com.meiyuemall.trade.domain.Order;
import com.meiyuemall.trade.domain.OrderItem;
import com.meiyuemall.trade.domain.OrderStatus;
import com.meiyuemall.trade.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * 售后闭环：申请 → 审核（48h 自动同意）→ 仅退款或退货退款（逆向签收后）→ 退款入账 → 关闭。
 */
@Service
public class AftersaleService {

    public static final int SELLER_REVIEW_HOURS = 48;

    private final AftersaleRepository aftersaleRepository;
    private final OrderRepository orderRepository;
    private final LogisticsService logisticsService;
    private final SettlementLedgerService settlementLedgerService;
    private final PaymentService paymentService;
    private final DelayTaskPort delayTaskPort;
    private final NotificationPublisher notificationPublisher;
    private final SellerOwnerLookup sellerOwnerLookup;

    public AftersaleService(
            AftersaleRepository aftersaleRepository,
            OrderRepository orderRepository,
            LogisticsService logisticsService,
            SettlementLedgerService settlementLedgerService,
            PaymentService paymentService,
            DelayTaskPort delayTaskPort,
            NotificationPublisher notificationPublisher,
            SellerOwnerLookup sellerOwnerLookup
    ) {
        this.aftersaleRepository = aftersaleRepository;
        this.orderRepository = orderRepository;
        this.logisticsService = logisticsService;
        this.settlementLedgerService = settlementLedgerService;
        this.paymentService = paymentService;
        this.delayTaskPort = delayTaskPort;
        this.notificationPublisher = notificationPublisher;
        this.sellerOwnerLookup = sellerOwnerLookup;
    }

    @Transactional
    public AftersaleResponse apply(ApplyAftersaleRequest request) {
        MeiyuePrincipal buyer = SecurityUtils.requirePrincipal();
        Order order = orderRepository.findByIdAndBuyerUserId(request.orderId(), buyer.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (order.getStatus() != OrderStatus.PAID
                && order.getStatus() != OrderStatus.FULFILLING
                && order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前订单状态不可申请售后");
        }
        OrderItem item = resolveItem(order, request.orderItemId());
        if (request.refundCents() > item.getLineTotalCents()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "退款金额不可超过行金额");
        }

        Aftersale as = new Aftersale();
        as.setAftersaleNo(genNo("A"));
        as.setOrderId(order.getId());
        as.setOrderItemId(item.getId());
        as.setTenantId(item.getTenantId());
        as.setBuyerUserId(buyer.getUserId());
        as.setType(request.type());
        as.setStatus(AftersaleStatus.REVIEWING);
        as.setReason(request.reason());
        as.setRefundCents(request.refundCents());
        as.setEvidenceImageUrls(joinUrls(request.evidenceImageUrls()));
        as.setSellerDeadlineAt(Instant.now().plus(SELLER_REVIEW_HOURS, ChronoUnit.HOURS));
        aftersaleRepository.save(as);
        // I9：Redis 延迟 48h 自动同意
        delayTaskPort.schedule(
                DelayTaskPort.TYPE_AFTERSALE_AUTO,
                String.valueOf(as.getId()),
                as.getSellerDeadlineAt()
        );
        // I11：售后申请 → 商家
        sellerOwnerLookup.findOwnerUserId(as.getTenantId()).ifPresent(ownerId ->
                notificationPublisher.publish(
                        ownerId, "SELLER",
                        "新售后待审核",
                        "售后单 " + as.getAftersaleNo() + " 待处理（" + as.getType() + "）",
                        "AFTERSALE", "AFTERSALE", String.valueOf(as.getId())
                )
        );
        return toResponse(as);
    }

    @Transactional
    public AftersaleResponse approve(Long id, ReviewAftersaleRequest request, boolean auto) {
        Aftersale as = loadForSellerOrAuto(id, auto);
        if (as.getStatus() != AftersaleStatus.REVIEWING && as.getStatus() != AftersaleStatus.APPLIED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "售后不在待审状态");
        }
        as.setStatus(AftersaleStatus.APPROVED);
        as.setReviewedAt(Instant.now());
        as.setReviewNote(request == null ? (auto ? "48h 超时自动同意" : null) : request.reviewNote());
        if (as.getType() == AftersaleType.REFUND_ONLY) {
            doRefundAndClose(as);
        }
        // RETURN_REFUND：等待买家填逆向运单 → 商家签收 → 退款
        notificationPublisher.publish(
                as.getBuyerUserId(), "BUYER",
                auto ? "售后已自动同意" : "售后已同意",
                "售后单 " + as.getAftersaleNo() + " 商家已同意"
                        + (as.getType() == AftersaleType.RETURN_REFUND ? "，请填写退货运单" : "，退款处理中"),
                "AFTERSALE", "AFTERSALE", String.valueOf(as.getId())
        );
        return toResponse(as);
    }

    @Transactional
    public AftersaleResponse reject(Long id, ReviewAftersaleRequest request) {
        Aftersale as = loadForSellerOrAuto(id, false);
        if (as.getStatus() != AftersaleStatus.REVIEWING && as.getStatus() != AftersaleStatus.APPLIED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "售后不在待审状态");
        }
        as.setStatus(AftersaleStatus.REJECTED);
        as.setReviewedAt(Instant.now());
        as.setReviewNote(request == null ? null : request.reviewNote());
        as.setClosedAt(Instant.now());
        as.setStatus(AftersaleStatus.CLOSED);
        notificationPublisher.publish(
                as.getBuyerUserId(), "BUYER",
                "售后已拒绝",
                "售后单 " + as.getAftersaleNo() + " 商家已拒绝"
                        + (as.getReviewNote() == null ? "" : "：" + as.getReviewNote()),
                "AFTERSALE", "AFTERSALE", String.valueOf(as.getId())
        );
        return toResponse(as);
    }

    @Transactional
    public AftersaleResponse fillReverseTracking(Long id, FillReverseTrackingRequest request) {
        MeiyuePrincipal buyer = SecurityUtils.requirePrincipal();
        Aftersale as = aftersaleRepository.findByIdAndBuyerUserId(id, buyer.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "售后单不存在"));
        if (as.getType() != AftersaleType.RETURN_REFUND) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅退货退款需填逆向运单");
        }
        if (as.getStatus() != AftersaleStatus.APPROVED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "需商家同意后再寄回");
        }
        Shipment reverse = logisticsService.createReverse(
                as.getTenantId(),
                as.getOrderId(),
                as.getId(),
                request.carrierCode(),
                request.trackingNo(),
                "商家退货仓",
                "",
                ""
        );
        as.setReverseShipmentId(reverse.getId());
        return toResponse(as);
    }

    /** 商家确认收到退货 → 退款关闭 */
    @Transactional
    public AftersaleResponse confirmReturnReceived(Long id) {
        Aftersale as = loadForSellerOrAuto(id, false);
        if (as.getType() != AftersaleType.RETURN_REFUND || as.getStatus() != AftersaleStatus.APPROVED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态不允许确认收货");
        }
        if (as.getReverseShipmentId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "买家尚未填写退货运单");
        }
        logisticsService.confirmReverseDelivered(as.getReverseShipmentId(), as.getTenantId());
        doRefundAndClose(as);
        return toResponse(as);
    }

    @Transactional
    public int autoApproveExpired() {
        List<Aftersale> list = aftersaleRepository.findByStatusInAndSellerDeadlineAtBefore(
                List.of(AftersaleStatus.REVIEWING, AftersaleStatus.APPLIED),
                Instant.now()
        );
        for (Aftersale as : list) {
            approve(as.getId(), new ReviewAftersaleRequest("48h 超时自动同意"), true);
        }
        return list.size();
    }

    /** Redis 延迟任务：按 ID 自动同意（幂等） */
    @Transactional
    public boolean autoApproveById(Long id) {
        Aftersale as = aftersaleRepository.findById(id).orElse(null);
        if (as == null) {
            return false;
        }
        if (as.getStatus() != AftersaleStatus.REVIEWING && as.getStatus() != AftersaleStatus.APPLIED) {
            return false;
        }
        if (as.getSellerDeadlineAt().isAfter(Instant.now())) {
            return false;
        }
        approve(id, new ReviewAftersaleRequest("48h 超时自动同意"), true);
        return true;
    }

    @Transactional(readOnly = true)
    public List<AftersaleResponse> listMineBuyer() {
        Long uid = SecurityUtils.requirePrincipal().getUserId();
        return aftersaleRepository.findByBuyerUserIdOrderByCreatedAtDesc(uid).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AftersaleResponse> listMineSeller() {
        Long tenantId = SecurityUtils.requirePrincipal().getTenantId();
        if (tenantId == null) throw new BusinessException(ErrorCode.TENANT_REQUIRED);
        return aftersaleRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(this::toResponse).toList();
    }

    private void doRefundAndClose(Aftersale as) {
        as.setStatus(AftersaleStatus.REFUNDING);
        // I12：先走通道退款（MOCK 幂等），再记结算账本；不做官方分账打款
        paymentService.refundByAftersale(as.getOrderId(), as.getId(), as.getRefundCents());
        settlementLedgerService.recordRefund(
                as.getTenantId(), as.getOrderId(), as.getOrderItemId(), as.getRefundCents(),
                "售后退款 " + as.getAftersaleNo()
        );
        as.setStatus(AftersaleStatus.CLOSED);
        as.setClosedAt(Instant.now());
    }

    private Aftersale loadForSellerOrAuto(Long id, boolean auto) {
        if (auto) {
            return aftersaleRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "售后单不存在"));
        }
        Long tenantId = SecurityUtils.requirePrincipal().getTenantId();
        if (tenantId == null) throw new BusinessException(ErrorCode.TENANT_REQUIRED);
        return aftersaleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "售后单不存在"));
    }

    private OrderItem resolveItem(Order order, Long orderItemId) {
        if (orderItemId != null) {
            return order.getItems().stream().filter(i -> i.getId().equals(orderItemId)).findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单行不存在"));
        }
        if (order.getItems().size() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "多行订单请指定 orderItemId");
        }
        return order.getItems().get(0);
    }

    private AftersaleResponse toResponse(Aftersale as) {
        return new AftersaleResponse(
                as.getId(), as.getAftersaleNo(), as.getOrderId(), as.getOrderItemId(), as.getTenantId(),
                as.getType().name(), as.getStatus().name(), as.getReason(), as.getRefundCents(),
                as.getSellerDeadlineAt().toString(), as.getReverseShipmentId(), as.getReviewNote(),
                splitUrls(as.getEvidenceImageUrls())
        );
    }

    /** 最多 6 个 URL，逗号拼接入库 */
    private static String joinUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return null;
        }
        return urls.stream()
                .filter(u -> u != null && !u.isBlank())
                .map(String::trim)
                .limit(6)
                .reduce((a, b) -> a + "," + b)
                .orElse(null);
    }

    private static List<String> splitUrls(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static String genNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
