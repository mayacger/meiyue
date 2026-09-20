package com.meiyuemall.trade.service;

import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductSku;
import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.repo.ProductSkuRepository;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.payment.domain.PaymentChannel;
import com.meiyuemall.payment.dto.PaymentResponse;
import com.meiyuemall.payment.service.PaymentService;
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
import java.util.List;
import java.util.UUID;

/**
 * 下单：校验 → 预占库存（扣减 stock_qty）→ 创建待支付订单 + 支付单。
 * 超时 30 分钟由 {@link OrderExpireJob} 关单并回滚库存。
 */
@Service
public class OrderService {

    public static final int PAY_TIMEOUT_MINUTES = 30;

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ProductSkuRepository productSkuRepository;
    private final PaymentService paymentService;

    public OrderService(
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            ProductSkuRepository productSkuRepository,
            PaymentService paymentService
    ) {
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.productSkuRepository = productSkuRepository;
        this.paymentService = paymentService;
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

        cartItemRepository.deleteByBuyerUserIdAndSkuIdIn(buyerId, skuIdsToClear);

        PaymentResponse payment = paymentService.createPending(order.getId(), total, PaymentChannel.MOCK);
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
                order.getPayExpireAt().toString(),
                order.getPaidAt() == null ? null : order.getPaidAt().toString(),
                items,
                paymentNo
        );
    }

    private static String genNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
