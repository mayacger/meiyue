package com.meiyuemall.logistics.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
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
import com.meiyuemall.logistics.repo.ShipmentRepository;
import com.meiyuemall.logistics.repo.ShipmentTrackRepository;
import com.meiyuemall.logistics.track.ExpressTrackQueryPort;
import com.meiyuemall.trade.domain.Order;
import com.meiyuemall.trade.domain.OrderItem;
import com.meiyuemall.trade.domain.OrderStatus;
import com.meiyuemall.trade.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 物流服务：正向发货 / 状态推进 / 轨迹同步；逆向运单创建供售后使用。
 */
@Service
public class LogisticsService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackRepository trackRepository;
    private final OrderRepository orderRepository;
    private final ExpressTrackQueryPort trackQueryPort;

    public LogisticsService(
            ShipmentRepository shipmentRepository,
            ShipmentTrackRepository trackRepository,
            OrderRepository orderRepository,
            ExpressTrackQueryPort trackQueryPort
    ) {
        this.shipmentRepository = shipmentRepository;
        this.trackRepository = trackRepository;
        this.orderRepository = orderRepository;
        this.trackQueryPort = trackQueryPort;
    }

    @Transactional
    public ShipmentResponse createForward(CreateForwardShipmentRequest request) {
        MeiyuePrincipal principal = requireSeller();
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.FULFILLING) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅已支付订单可发货");
        }
        // 防越权：订单行必须属于本租户
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

        Shipment shipment = new Shipment();
        shipment.setTenantId(principal.getTenantId());
        shipment.setOrderId(order.getId());
        shipment.setOrderItemId(request.orderItemId());
        shipment.setDirection(ShipmentDirection.FORWARD);
        shipment.setCarrierCode(request.carrierCode());
        shipment.setTrackingNo(request.trackingNo());
        shipment.setStatus(ForwardStatus.PENDING_PICKUP.name());
        shipment.setReceiverName(request.receiverName());
        shipment.setReceiverPhone(request.receiverPhone());
        shipment.setReceiverAddress(request.receiverAddress());
        shipmentRepository.save(shipment);
        appendTrack(shipment, ForwardStatus.PENDING_PICKUP.name(), "商家已填运单，待揽收", "MANUAL");

        if (order.getStatus() == OrderStatus.PAID) {
            order.setStatus(OrderStatus.FULFILLING);
        }
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
                maybeCompleteOrder(shipment.getOrderId());
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
            // 尝试推进正向状态（忽略非法流转）
            if (shipment.getDirection() == ShipmentDirection.FORWARD) {
                try {
                    ForwardStatus cur = ForwardStatus.valueOf(shipment.getStatus());
                    ForwardStatus to = ForwardStatus.valueOf(node.status());
                    if (cur.canTransitTo(to) && cur != to) {
                        shipment.setStatus(to.name());
                    }
                } catch (Exception ignored) {
                    // 轨迹状态与本地枚举不一致时仅记轨迹
                }
            }
        }
        return toResponse(shipment);
    }

    /**
     * 售后创建逆向运单（买家寄回）。
     */
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
        ReverseStatus cur = ReverseStatus.valueOf(shipment.getStatus());
        if (!cur.canTransitTo(ReverseStatus.DELIVERED_TO_SELLER) && cur != ReverseStatus.DELIVERED_TO_SELLER) {
            // 允许从途中直接确认签收（人工校正）
            shipment.setStatus(ReverseStatus.DELIVERED_TO_SELLER.name());
        } else {
            shipment.setStatus(ReverseStatus.DELIVERED_TO_SELLER.name());
        }
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
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Shipment requireById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "运单不存在"));
    }

    private void maybeCompleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return;
        List<Shipment> forwards = shipmentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .filter(s -> s.getDirection() == ShipmentDirection.FORWARD)
                .toList();
        if (forwards.isEmpty()) return;
        boolean allDelivered = forwards.stream()
                .allMatch(s -> ForwardStatus.DELIVERED.name().equals(s.getStatus()));
        if (allDelivered) {
            order.setStatus(OrderStatus.COMPLETED);
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
                s.getAftersaleId(), tracks
        );
    }
}
