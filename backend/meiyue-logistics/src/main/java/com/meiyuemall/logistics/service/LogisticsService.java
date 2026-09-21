package com.meiyuemall.logistics.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.notify.NotificationPublisher;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.logistics.domain.ForwardStatus;
import com.meiyuemall.logistics.domain.ReverseStatus;
import com.meiyuemall.logistics.domain.Shipment;
import com.meiyuemall.logistics.domain.ShipmentDirection;
import com.meiyuemall.logistics.domain.ShipmentTrack;
import com.meiyuemall.logistics.dto.CreateForwardShipmentRequest;
import com.meiyuemall.logistics.dto.ShipmentResponse;
import com.meiyuemall.logistics.dto.ShipmentTrackResponse;
import com.meiyuemall.logistics.dto.UpdateShipmentStatusRequest;
import com.meiyuemall.logistics.ewaybill.EwaybillProvider;
import com.meiyuemall.logistics.repo.ShipmentRepository;
import com.meiyuemall.logistics.repo.ShipmentTrackRepository;
import com.meiyuemall.logistics.track.ExpressTrackQueryPort;
import com.meiyuemall.trade.domain.Order;
import com.meiyuemall.trade.domain.OrderItem;
import com.meiyuemall.trade.domain.OrderStatus;
import com.meiyuemall.trade.repo.OrderRepository;
import com.meiyuemall.trade.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 物流服务：正向发货 / 多包裹 / MOCK 电子面单 / 状态推进 / 轨迹同步。
 * I32：全部签收后委托 OrderService 进入自动确认窗口。
 */
@Service
public class LogisticsService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackRepository trackRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final ExpressTrackQueryPort trackQueryPort;
    private final EwaybillProvider ewaybillProvider;
    private final NotificationPublisher notificationPublisher;

    public LogisticsService(
            ShipmentRepository shipmentRepository,
            ShipmentTrackRepository trackRepository,
            OrderRepository orderRepository,
            OrderService orderService,
            ExpressTrackQueryPort trackQueryPort,
            EwaybillProvider ewaybillProvider,
            NotificationPublisher notificationPublisher
    ) {
        this.shipmentRepository = shipmentRepository;
        this.trackRepository = trackRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.trackQueryPort = trackQueryPort;
        this.ewaybillProvider = ewaybillProvider;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional
    public ShipmentResponse createForward(CreateForwardShipmentRequest request) {
        MeiyuePrincipal principal = requireSeller();
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.FULFILLING) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅已支付订单可发货");
        }
        boolean belongs = order.getItems().stream().anyMatch(i -> principal.getTenantId().equals(i.getTenantId()));
        if (!belongs) {
            throw new BusinessException(ErrorCode.TENANT_MISMATCH);
        }
        if (request.orderItemId() != null) {
            OrderItem item = order.getItems().stream()
                    .filter(i -> i.getId().equals(request.orderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单行不存在"));
            if (!principal.getTenantId().equals(item.getTenantId())) {
                throw new BusinessException(ErrorCode.TENANT_MISMATCH);
            }
        }

        // 多包裹：未传 packageSeq 则取本店本单已有 FORWARD 数 + 1
        int seq = request.packageSeq() != null && request.packageSeq() > 0
                ? request.packageSeq()
                : nextPackageSeq(order.getId(), principal.getTenantId());

        String carrier = request.carrierCode();
        String tracking = request.trackingNo();
        String ewaybillNo = null;
        String labelUrl = null;
        String ewaybillProviderName = null;

        // MOCK 打单：可生成运单号/面单；若客户端已填 trackingNo 则保留
        boolean print = request.printEwaybill() == null || Boolean.TRUE.equals(request.printEwaybill());
        if (print) {
            EwaybillProvider.EwaybillResult eb = ewaybillProvider.print(carrier, order.getId(), seq);
            ewaybillNo = eb.ewaybillNo();
            labelUrl = eb.labelUrl();
            ewaybillProviderName = eb.provider();
            if (tracking == null || tracking.isBlank()) {
                tracking = ewaybillNo;
            }
        }
        if (tracking == null || tracking.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "运单号不能为空（可开启 printEwaybill 自动生成）");
        }

        Shipment shipment = new Shipment();
        shipment.setTenantId(principal.getTenantId());
        shipment.setOrderId(order.getId());
        shipment.setOrderItemId(request.orderItemId());
        shipment.setDirection(ShipmentDirection.FORWARD);
        shipment.setCarrierCode(carrier);
        shipment.setTrackingNo(tracking);
        shipment.setPackageSeq(seq);
        shipment.setEwaybillNo(ewaybillNo);
        shipment.setEwaybillLabelUrl(labelUrl);
        shipment.setEwaybillProvider(ewaybillProviderName);
        shipment.setStatus(ForwardStatus.PENDING_PICKUP.name());
        shipment.setReceiverName(request.receiverName());
        shipment.setReceiverPhone(request.receiverPhone());
        shipment.setReceiverAddress(request.receiverAddress());
        shipmentRepository.save(shipment);
        appendTrack(shipment, ForwardStatus.PENDING_PICKUP.name(),
                "商家已发货（包裹#" + seq + "）" + (ewaybillNo != null ? "，面单 " + ewaybillNo : ""),
                "MANUAL");

        if (order.getStatus() == OrderStatus.PAID) {
            order.setStatus(OrderStatus.FULFILLING);
        }

        // I11：发货通知买家
        notificationPublisher.publish(
                order.getBuyerUserId(), "BUYER",
                "订单已发货",
                "订单 " + order.getOrderNo() + " 包裹#" + seq + " 已发出，运单 " + tracking,
                "SHIPMENT", "SHIPMENT", String.valueOf(shipment.getId())
        );
        return toResponse(shipment);
    }

    /** 对已有运单补打 MOCK 电子面单 */
    @Transactional
    public ShipmentResponse printEwaybill(Long shipmentId) {
        MeiyuePrincipal principal = requireSeller();
        Shipment shipment = shipmentRepository.findByIdAndTenantId(shipmentId, principal.getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "运单不存在"));
        EwaybillProvider.EwaybillResult eb = ewaybillProvider.print(
                shipment.getCarrierCode(), shipment.getOrderId(), shipment.getPackageSeq());
        shipment.setEwaybillNo(eb.ewaybillNo());
        shipment.setEwaybillLabelUrl(eb.labelUrl());
        shipment.setEwaybillProvider(eb.provider());
        if (shipment.getTrackingNo() == null || shipment.getTrackingNo().isBlank()) {
            shipment.setTrackingNo(eb.ewaybillNo());
        }
        appendTrack(shipment, shipment.getStatus(), "MOCK 电子面单已生成 " + eb.ewaybillNo(), "MANUAL");
        return toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse updateStatus(Long shipmentId, UpdateShipmentStatusRequest request) {
        MeiyuePrincipal principal = requireSeller();
        Shipment shipment = shipmentRepository.findByIdAndTenantId(shipmentId, principal.getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "运单不存在"));
        String next = request.status().trim().toUpperCase();
        if (shipment.getDirection() == ShipmentDirection.FORWARD) {
            ForwardStatus cur = ForwardStatus.valueOf(shipment.getStatus());
            ForwardStatus to = ForwardStatus.valueOf(next);
            if (!cur.canTransitTo(to)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "非法正向状态流转: " + cur + " → " + to);
            }
            shipment.setStatus(to.name());
            appendTrack(shipment, to.name(),
                    request.description() == null ? ("状态变更为 " + to) : request.description(), "MANUAL");
            if (to == ForwardStatus.DELIVERED) {
                // I32：全部签收后进入自动确认窗口（不再立刻 COMPLETED）
                maybeMarkDeliveredForAutoConfirm(shipment.getOrderId());
                orderRepository.findById(shipment.getOrderId()).ifPresent(order ->
                        notificationPublisher.publish(
                                order.getBuyerUserId(), "BUYER",
                                "包裹已送达",
                                "订单 " + order.getOrderNo() + " 包裹#" + shipment.getPackageSeq() + " 已签收",
                                "SHIPMENT", "SHIPMENT", String.valueOf(shipment.getId())
                        )
                );
            }
        } else {
            ReverseStatus cur = ReverseStatus.valueOf(shipment.getStatus());
            ReverseStatus to = ReverseStatus.valueOf(next);
            if (!cur.canTransitTo(to)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "非法逆向状态流转: " + cur + " → " + to);
            }
            shipment.setStatus(to.name());
            appendTrack(shipment, to.name(),
                    request.description() == null ? ("状态变更为 " + to) : request.description(), "MANUAL");
        }
        return toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse syncTracks(Long shipmentId) {
        MeiyuePrincipal principal = requireSeller();
        Shipment shipment = shipmentRepository.findByIdAndTenantId(shipmentId, principal.getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "运单不存在"));
        var result = trackQueryPort.query(shipment.getCarrierCode(), shipment.getTrackingNo());
        if (!result.found()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "暂无轨迹（占位查询未命中）");
        }
        for (var node : result.nodes()) {
            appendTrack(shipment, node.status(), node.description(), "QUERY");
            if (shipment.getDirection() == ShipmentDirection.FORWARD) {
                try {
                    ForwardStatus cur = ForwardStatus.valueOf(shipment.getStatus());
                    ForwardStatus to = ForwardStatus.valueOf(node.status());
                    if (cur.canTransitTo(to) && cur != to) {
                        shipment.setStatus(to.name());
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return toResponse(shipment);
    }

    @Transactional
    public Shipment createReverse(Long tenantId, Long orderId, Long aftersaleId,
                                  String carrierCode, String trackingNo,
                                  String receiverName, String receiverPhone, String receiverAddress) {
        Shipment shipment = new Shipment();
        shipment.setTenantId(tenantId);
        shipment.setOrderId(orderId);
        shipment.setDirection(ShipmentDirection.REVERSE);
        shipment.setCarrierCode(carrierCode);
        shipment.setTrackingNo(trackingNo);
        shipment.setPackageSeq(1);
        shipment.setStatus(ReverseStatus.PENDING_SEND.name());
        shipment.setAftersaleId(aftersaleId);
        shipment.setReceiverName(receiverName);
        shipment.setReceiverPhone(receiverPhone);
        shipment.setReceiverAddress(receiverAddress);
        shipmentRepository.save(shipment);
        appendTrack(shipment, ReverseStatus.PENDING_SEND.name(), "买家已填退货运单，待寄出", "MANUAL");
        return shipment;
    }

    @Transactional
    public ShipmentResponse confirmReverseDelivered(Long shipmentId, Long tenantId) {
        Shipment shipment = shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "运单不存在"));
        if (shipment.getDirection() != ShipmentDirection.REVERSE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非逆向运单");
        }
        shipment.setStatus(ReverseStatus.DELIVERED_TO_SELLER.name());
        appendTrack(shipment, ReverseStatus.DELIVERED_TO_SELLER.name(), "商家确认收到退货", "MANUAL");
        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> listByOrder(Long orderId) {
        return shipmentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> listMineForward() {
        Long tenantId = requireSeller().getTenantId();
        return shipmentRepository.findByTenantIdAndDirectionOrderByCreatedAtDesc(tenantId, ShipmentDirection.FORWARD)
                .stream().map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Shipment requireById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "运单不存在"));
    }

    private int nextPackageSeq(Long orderId, Long tenantId) {
        long count = shipmentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .filter(s -> s.getDirection() == ShipmentDirection.FORWARD)
                .filter(s -> tenantId.equals(s.getTenantId()))
                .count();
        return (int) count + 1;
    }

    private void maybeMarkDeliveredForAutoConfirm(Long orderId) {
        List<Shipment> forwards = shipmentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .filter(s -> s.getDirection() == ShipmentDirection.FORWARD)
                .toList();
        if (forwards.isEmpty()) {
            return;
        }
        boolean allDelivered = forwards.stream()
                .allMatch(s -> ForwardStatus.DELIVERED.name().equals(s.getStatus()));
        if (allDelivered) {
            orderService.onAllPackagesDelivered(orderId);
        }
    }

    private void appendTrack(Shipment shipment, String status, String description, String source) {
        ShipmentTrack track = new ShipmentTrack();
        track.setShipmentId(shipment.getId());
        track.setStatus(status);
        track.setDescription(description);
        track.setSource(source);
        track.setTrackedAt(Instant.now());
        trackRepository.save(track);
    }

    private MeiyuePrincipal requireSeller() {
        MeiyuePrincipal p = SecurityUtils.requirePrincipal();
        if (p.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED);
        }
        return p;
    }

    private ShipmentResponse toResponse(Shipment s) {
        List<ShipmentTrackResponse> tracks = trackRepository.findByShipmentIdOrderByTrackedAtAsc(s.getId())
                .stream()
                .map(t -> new ShipmentTrackResponse(
                        t.getId(), t.getStatus(), t.getDescription(), t.getSource(), t.getTrackedAt().toString()))
                .toList();
        return new ShipmentResponse(
                s.getId(), s.getTenantId(), s.getOrderId(), s.getOrderItemId(),
                s.getDirection().name(), s.getCarrierCode(), s.getTrackingNo(), s.getStatus(),
                s.getAftersaleId(), s.getPackageSeq(), s.getEwaybillNo(), s.getEwaybillLabelUrl(),
                s.getEwaybillProvider(), tracks
        );
    }
}
