package com.meiyuemall.trade.service;

import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductSku;
import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.repo.ProductSkuRepository;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.notify.NotificationPublisher;
import com.meiyuemall.common.redis.DelayTaskPort;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.common.tenant.SellerOwnerLookup;
import com.meiyuemall.payment.domain.PaymentChannel;
import com.meiyuemall.payment.dto.PaymentResponse;
import com.meiyuemall.payment.service.PaymentService;
import com.meiyuemall.platform.service.PlatformConfigService;
import com.meiyuemall.trade.domain.CartItem;
import com.meiyuemall.trade.domain.Order;
import com.meiyuemall.trade.domain.OrderItem;
import com.meiyuemall.trade.domain.OrderStatus;
import com.meiyuemall.trade.dto.CheckoutRequest;
import com.meiyuemall.trade.dto.OrderItemResponse;
import com.meiyuemall.trade.dto.OrderResponse;
import com.meiyuemall.trade.repo.CartItemRepository;
import com.meiyuemall.trade.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * 下单：校验 → 预占库存 → 待支付订单 + 支付单。
 * I32：买家取消与超时关单共用 {@link #cancelOrder}；签收后 N 天自动确认。
 * I33：下单备注与发票快照。
 */
@Service
public class OrderService {

    public static final int PAY_TIMEOUT_MINUTES = 30;
    public static final int DEFAULT_AUTO_CONFIRM_DAYS = 7;

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ProductSkuRepository productSkuRepository;
    private final PaymentService paymentService;
    private final DelayTaskPort delayTaskPort;
    private final CouponService couponService;
    private final PlatformCouponService platformCouponService;
    private final String couponStackingMode;
    private final NotificationPublisher notificationPublisher;
    private final SellerOwnerLookup sellerOwnerLookup;
    private final FreightService freightService;
    private final PlatformConfigService platformConfigService;

    public OrderService(
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            ProductSkuRepository productSkuRepository,
            PaymentService paymentService,
            DelayTaskPort delayTaskPort,
            CouponService couponService,
            PlatformCouponService platformCouponService,
            @org.springframework.beans.factory.annotation.Value("${meiyue.coupon.stacking:MUTUAL_EXCLUSIVE}") String couponStackingMode,
            NotificationPublisher notificationPublisher,
            SellerOwnerLookup sellerOwnerLookup,
            FreightService freightService,
            PlatformConfigService platformConfigService
    ) {
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.productSkuRepository = productSkuRepository;
        this.paymentService = paymentService;
        this.delayTaskPort = delayTaskPort;
        this.couponService = couponService;
        this.platformCouponService = platformCouponService;
        this.couponStackingMode = couponStackingMode;
        this.notificationPublisher = notificationPublisher;
        this.sellerOwnerLookup = sellerOwnerLookup;
        this.freightService = freightService;
        this.platformConfigService = platformConfigService;
    }

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        List<CartItem> cartItems = cartItemRepository.findByBuyerUserIdOrderByUpdatedAtDesc(buyerId);
        if (request != null && request.cartItemIds() != null && !request.cartItemIds().isEmpty()) {
            cartItems = cartItems.stream()
                    .filter(c -> request.cartItemIds().contains(c.getId()))
                    .toList();
        }
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "购物车为空");
        }

        Order order = new Order();
        order.setOrderNo(genNo("O"));
        order.setBuyerUserId(buyerId);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setPayExpireAt(Instant.now().plus(PAY_TIMEOUT_MINUTES, ChronoUnit.MINUTES));
        // I33：备注与发票快照
        if (request != null) {
            if (request.buyerRemark() != null && !request.buyerRemark().isBlank()) {
                order.setBuyerRemark(request.buyerRemark().trim());
            }
            applyInvoiceSnapshot(order, request);
        }

        long total = 0;
        List<Long> skuIdsToClear = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            ProductSku sku = productSkuRepository.findByIdForUpdate(cartItem.getSkuId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "SKU 不存在: " + cartItem.getSkuId()));
            Product product = sku.getProduct();
            if (product == null || product.getStatus() != ProductStatus.ON_SALE) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "商品不可售: " + cartItem.getProductId());
            }
            if (sku.getStockQty() < cartItem.getQuantity()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "库存不足: " + sku.getSkuCode());
            }
            // 预占：直接扣减可售库存；超时/取消时回滚
            sku.setStockQty(sku.getStockQty() - cartItem.getQuantity());

            OrderItem line = new OrderItem();
            line.setOrder(order);
            line.setTenantId(product.getTenantId());
            line.setProductId(product.getId());
            line.setSkuId(sku.getId());
            line.setProductTitle(product.getTitle());
            line.setSkuCode(sku.getSkuCode());
            line.setSpecText(sku.getSpecText());
            line.setUnitPriceCents(sku.getPriceCents());
            line.setQuantity(cartItem.getQuantity());
            line.setLineTotalCents(sku.getPriceCents() * cartItem.getQuantity());
            order.getItems().add(line);
            total += line.getLineTotalCents();
            skuIdsToClear.add(sku.getId());
        }
        order.setTotalCents(total);
        orderRepository.save(order);

        Long storeClaimId = request == null ? null : request.resolvedStoreClaimId();
        Long platformClaimId = request == null ? null : request.platformCouponClaimId();
        CouponStackingRules.assertExclusive(storeClaimId, platformClaimId, couponStackingMode);

        // 店券抵扣：仅单店订单（I12 跨店规则）
        if (storeClaimId != null) {
            Long couponTenant = order.getItems().get(0).getTenantId();
            long tenantSubtotal = order.getItems().stream()
                    .filter(i -> couponTenant.equals(i.getTenantId()))
                    .mapToLong(OrderItem::getLineTotalCents).sum();
            boolean singleTenant = order.getItems().stream().map(OrderItem::getTenantId).distinct().count() == 1;
            CouponStackingRules.assertStoreCouponSingleShop(singleTenant);
            long discount = couponService.applyClaimToOrder(
                    storeClaimId, buyerId, couponTenant, tenantSubtotal, order.getId());
            total = Math.max(1, total - discount);
            order.setTotalCents(total);
            orderRepository.save(order);
        }

        // 平台券抵扣：整单门槛；跨店可用；与店券互斥
        if (platformClaimId != null) {
            long discount = platformCouponService.applyClaimToOrder(
                    platformClaimId, buyerId, total, order.getId());
            total = Math.max(1, total - discount);
            order.setTotalCents(total);
            orderRepository.save(order);
        }

        // I31：券后商品金额 + 按店运费
        long goodsCents = total;
        var freightEst = freightService.estimateFromLines(
                order.getItems().stream()
                        .map(i -> new FreightService.Line(i.getTenantId(), i.getLineTotalCents()))
                        .toList()
        );
        // 运费按券前各店小计估算（MVP）；包邮门槛对照行小计
        long freightCents = freightEst.freightCents();
        order.setGoodsCents(goodsCents);
        order.setFreightCents(freightCents);
        total = goodsCents + freightCents;
        order.setTotalCents(total);
        orderRepository.save(order);

        // I9：Redis 延迟关单（DB 扫描仍作兜底）
        delayTaskPort.schedule(DelayTaskPort.TYPE_ORDER_EXPIRE, String.valueOf(order.getId()), order.getPayExpireAt());

        cartItemRepository.deleteByBuyerUserIdAndSkuIdIn(buyerId, skuIdsToClear);

        PaymentResponse payment = paymentService.createPending(order.getId(), total, PaymentChannel.MOCK);
        // I11：下单成功 → 买家通知
        notificationPublisher.publish(
                buyerId, "BUYER",
                "下单成功",
                "订单 " + order.getOrderNo() + " 已创建，请尽快支付",
                "ORDER", "ORDER", String.valueOf(order.getId())
        );
        return toResponse(order, payment.paymentNo());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listMine() {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        return orderRepository.findByBuyerUserIdOrderByCreatedAtDesc(buyerId).stream()
                .map(o -> toResponse(o, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMine(Long orderId) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        Order order = orderRepository.findByIdAndBuyerUserId(orderId, buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        return toResponse(order, null);
    }

    /**
     * 买家确认收货 → COMPLETED（评价前置条件）。
     * 允许从 PAID / FULFILLING 确认（物流未全闭环时的人工确认）。
     */
    @Transactional
    public OrderResponse confirmReceipt(Long orderId) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        Order order = orderRepository.findByIdAndBuyerUserId(orderId, buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (order.getStatus() == OrderStatus.COMPLETED) {
            return toResponse(order, null);
        }
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.FULFILLING) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不可确认收货");
        }
        markCompleted(order, false);
        return toResponse(order, null);
    }

    /**
     * I32：买家取消未支付订单；与超时关单共用 {@link #cancelOrder} 释放预占库存。
     */
    @Transactional
    public OrderResponse cancelMine(Long orderId) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        Order order = orderRepository.findByIdAndBuyerUserId(orderId, buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅待支付订单可取消");
        }
        cancelOrder(order, "买家取消");
        return toResponse(order, null);
    }

    /**
     * I32：全部正向包裹签收后进入待自动确认（不再立刻 COMPLETED）。
     * days≤0 时立即确认并通知买家。
     */
    @Transactional
    public void onAllPackagesDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return;
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }
        Instant now = Instant.now();
        if (order.getDeliveredAt() == null) {
            order.setDeliveredAt(now);
        }
        int days = Math.max(0, platformConfigService.getInt(
                PlatformConfigService.KEY_AUTO_CONFIRM_DAYS, DEFAULT_AUTO_CONFIRM_DAYS));
        if (days <= 0) {
            markCompleted(order, true);
            return;
        }
        order.setAutoConfirmAt(order.getDeliveredAt().plus(days, ChronoUnit.DAYS));
        if (order.getStatus() == OrderStatus.PAID) {
            order.setStatus(OrderStatus.FULFILLING);
        }
    }

    /** I32：扫描到期自动确认 */
    @Transactional
    public int autoConfirmDueOrders() {
        List<Order> due = orderRepository.findByStatusAndAutoConfirmAtBefore(
                OrderStatus.FULFILLING, Instant.now());
        int n = 0;
        for (Order order : due) {
            markCompleted(order, true);
            n++;
        }
        return n;
    }

    /**
     * 标记完成；notifyBuyer=true 时发「自动确认收货」通知（I32），否则通知商家（买家手动确认）。
     */
    private void markCompleted(Order order, boolean autoConfirm) {
        if (order.getStatus() == OrderStatus.COMPLETED) {
            return;
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.setAutoConfirmAt(null);
        if (autoConfirm) {
            notificationPublisher.publish(
                    order.getBuyerUserId(), "BUYER",
                    "已自动确认收货",
                    "订单 " + order.getOrderNo() + " 已达签收时限，系统已确认收货，您可评价",
                    "ORDER", "ORDER", String.valueOf(order.getId())
            );
            order.getItems().stream().map(OrderItem::getTenantId).distinct().forEach(tenantId ->
                    sellerOwnerLookup.findOwnerUserId(tenantId).ifPresent(ownerId ->
                            notificationPublisher.publish(
                                    ownerId, "SELLER",
                                    "订单已自动确认收货",
                                    "订单 " + order.getOrderNo() + " 已自动完成",
                                    "ORDER", "ORDER", String.valueOf(order.getId())
                            )
                    )
            );
        } else {
            order.getItems().stream().map(OrderItem::getTenantId).distinct().forEach(tenantId ->
                    sellerOwnerLookup.findOwnerUserId(tenantId).ifPresent(ownerId ->
                            notificationPublisher.publish(
                                    ownerId, "SELLER",
                                    "买家已确认收货",
                                    "订单 " + order.getOrderNo() + " 已完成，买家可评价",
                                    "ORDER", "ORDER", String.valueOf(order.getId())
                            )
                    )
            );
        }
    }

    private void applyInvoiceSnapshot(Order order, CheckoutRequest request) {
        if (request.invoiceTitle() != null && !request.invoiceTitle().isBlank()) {
            order.setInvoiceTitle(request.invoiceTitle().trim());
            order.setInvoiceTaxNo(request.invoiceTaxNo() == null || request.invoiceTaxNo().isBlank()
                    ? null : request.invoiceTaxNo().trim());
            String type = request.invoiceType() == null || request.invoiceType().isBlank()
                    ? "PERSONAL" : request.invoiceType().trim().toUpperCase();
            if (!"PERSONAL".equals(type) && !"COMPANY".equals(type)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "invoiceType 须为 PERSONAL 或 COMPANY");
            }
            order.setInvoiceType(type);
        }
    }

    /** 模拟支付成功：标记订单 PAID（幂等） */
    @Transactional
    public OrderResponse mockPay(Long orderId) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        Order order = orderRepository.findByIdAndBuyerUserId(orderId, buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (order.getStatus() == OrderStatus.PAID) {
            return toResponse(order, null);
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单不可支付");
        }
        if (order.getPayExpireAt().isBefore(Instant.now())) {
            cancelOrder(order, "支付超时");
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单已超时关闭");
        }
        // 复用下单时创建的最新支付单
        var existing = paymentService.findLatestByOrderId(order.getId());
        String paymentNo = existing != null
                ? existing.paymentNo()
                : paymentService.createPending(order.getId(), order.getTotalCents(), PaymentChannel.MOCK).paymentNo();
        PaymentResponse paid = paymentService.mockPaySuccess(paymentNo);
        return toResponse(order, paid.paymentNo());
    }

    @Transactional
    public void markPaid(Order order) {
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(Instant.now());
    }

    @Transactional
    public void markPaidByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        markPaid(order);
    }

    /**
     * 关单并回滚库存（超时任务 / 主动取消共用）。
     */
    @Transactional
    public void cancelOrder(Order order, String reason) {
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return;
        }
        for (OrderItem line : order.getItems()) {
            productSkuRepository.findByIdForUpdate(line.getSkuId()).ifPresent(sku ->
                    sku.setStockQty(sku.getStockQty() + line.getQuantity())
            );
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancelReason(reason);
    }

    @Transactional
    public int cancelExpiredOrders() {
        List<Order> expired = orderRepository.findByStatusAndPayExpireAtBefore(
                OrderStatus.PENDING_PAYMENT, Instant.now());
        for (Order order : expired) {
            cancelOrder(order, "支付超时自动关单");
        }
        return expired.size();
    }

    /** 按订单 ID 关单（Redis 延迟任务消费；幂等） */
    @Transactional
    public boolean cancelExpiredById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return false;
        }
        if (order.getPayExpireAt().isAfter(Instant.now())) {
            return false;
        }
        cancelOrder(order, "支付超时自动关单");
        return true;
    }

    /**
     * 商家视角：本店有行的履约相关订单。
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> listForSeller() {
        Long tenantId = requireSellerTenant();
        return orderRepository.findDistinctForSeller(
                tenantId,
                EnumSet.of(OrderStatus.PAID, OrderStatus.FULFILLING, OrderStatus.COMPLETED, OrderStatus.PENDING_PAYMENT)
        ).stream().map(o -> toResponse(o, null)).toList();
    }

    /**
     * I22：导出本店已支付订单 CSV（UTF-8 BOM，Excel 友好）。
     * 列：orderNo,status,buyerUserId,totalCents,paidAt,itemTitles,itemQty
     */
    @Transactional(readOnly = true)
    public String exportPaidCsvForSeller() {
        Long tenantId = requireSellerTenant();
        List<Order> orders = orderRepository.findPaidForSeller(tenantId);
        return buildPaidOrdersCsv(orders, tenantId);
    }

    /**
     * I22：平台导出全站已支付订单 CSV。
     */
    @Transactional(readOnly = true)
    public String exportPaidCsvForAdmin() {
        List<Order> orders = orderRepository.findByPaidAtIsNotNullOrderByPaidAtDesc();
        return buildPaidOrdersCsv(orders, null);
    }

    /**
     * 生成已支付订单 CSV。
     * @param tenantId 非空时仅汇总该店订单行；Admin 传 null 汇总全部行
     */
    private String buildPaidOrdersCsv(List<Order> orders, Long tenantId) {
        StringBuilder sb = new StringBuilder();
        // BOM 便于 Excel 识别 UTF-8
        sb.append('\uFEFF');
        sb.append("orderNo,status,buyerUserId,totalCents,paidAt,itemTitles,itemQty\n");
        for (Order o : orders) {
            String titles;
            int qty;
            if (tenantId != null) {
                var lines = o.getItems().stream().filter(i -> tenantId.equals(i.getTenantId())).toList();
                titles = lines.stream()
                        .map(i -> escapeCsv(i.getProductTitle()))
                        .reduce((a, b) -> a + "|" + b)
                        .orElse("");
                qty = lines.stream().mapToInt(i -> i.getQuantity()).sum();
            } else {
                titles = o.getItems().stream()
                        .map(i -> escapeCsv(i.getProductTitle()))
                        .reduce((a, b) -> a + "|" + b)
                        .orElse("");
                qty = o.getItems().stream().mapToInt(i -> i.getQuantity()).sum();
            }
            sb.append(escapeCsv(o.getOrderNo())).append(',')
                    .append(o.getStatus().name()).append(',')
                    .append(o.getBuyerUserId()).append(',')
                    .append(o.getTotalCents()).append(',')
                    .append(o.getPaidAt() == null ? "" : o.getPaidAt().toString()).append(',')
                    .append(titles).append(',')
                    .append(qty)
                    .append('\n');
        }
        return sb.toString();
    }

    private static String escapeCsv(String raw) {
        if (raw == null) {
            return "";
        }
        String v = raw.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("|")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    private Long requireSellerTenant() {
        MeiyuePrincipal p = SecurityUtils.requirePrincipal();
        if (p.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED);
        }
        return p.getTenantId();
    }

    /**
     * I31：当前购物车运费预估（登录买家）。
     */
    @Transactional(readOnly = true)
    public com.meiyuemall.trade.dto.FreightEstimateResponse estimateFreightForCart(List<Long> cartItemIds) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        List<CartItem> cartItems = cartItemRepository.findByBuyerUserIdOrderByUpdatedAtDesc(buyerId);
        if (cartItemIds != null && !cartItemIds.isEmpty()) {
            cartItems = cartItems.stream().filter(c -> cartItemIds.contains(c.getId())).toList();
        }
        List<FreightService.Line> lines = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            ProductSku sku = productSkuRepository.findById(cartItem.getSkuId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "SKU 不存在"));
            Product product = sku.getProduct();
            if (product == null) {
                continue;
            }
            lines.add(new FreightService.Line(
                    product.getTenantId(),
                    sku.getPriceCents() * cartItem.getQuantity()
            ));
        }
        return freightService.estimateFromLines(lines);
    }

    private OrderResponse toResponse(Order order, String paymentNo) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(), i.getTenantId(), i.getProductId(), i.getSkuId(),
                        i.getProductTitle(), i.getSkuCode(), i.getSpecText(),
                        i.getUnitPriceCents(), i.getQuantity(), i.getLineTotalCents()
                )).toList();
        return new OrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getStatus().name(),
                order.getTotalCents(),
                order.getGoodsCents(),
                order.getFreightCents(),
                order.getPayExpireAt().toString(),
                order.getPaidAt() == null ? null : order.getPaidAt().toString(),
                items,
                paymentNo,
                order.getBuyerRemark(),
                order.getInvoiceTitle(),
                order.getInvoiceTaxNo(),
                order.getInvoiceType(),
                order.getDeliveredAt() == null ? null : order.getDeliveredAt().toString(),
                order.getAutoConfirmAt() == null ? null : order.getAutoConfirmAt().toString()
        );
    }

    private static String genNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
