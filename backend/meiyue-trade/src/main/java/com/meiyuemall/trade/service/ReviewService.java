package com.meiyuemall.trade.service;

import com.meiyuemall.trade.domain.ProductReview;
import com.meiyuemall.trade.dto.CreateReviewRequest;
import com.meiyuemall.trade.dto.ProductReviewResponse;
import com.meiyuemall.trade.repo.ProductReviewRepository;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.trade.domain.Order;
import com.meiyuemall.trade.domain.OrderItem;
import com.meiyuemall.trade.domain.OrderStatus;
import com.meiyuemall.trade.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 商品评价：订单 COMPLETED 后可评；商品详情公开；商家可见并可回复。
 */
@Service
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ProductReviewRepository reviewRepository, OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ProductReviewResponse create(CreateReviewRequest request) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        Order order = orderRepository.findByIdAndBuyerUserId(request.orderId(), buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "确认收货（订单完成）后方可评价");
        }
        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(request.orderItemId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单行不存在"));
        if (reviewRepository.existsByOrderItemId(item.getId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "该订单行已评价");
        }
        ProductReview r = new ProductReview();
        r.setProductId(item.getProductId());
        r.setOrderId(order.getId());
        r.setOrderItemId(item.getId());
        r.setTenantId(item.getTenantId());
        r.setBuyerUserId(buyerId);
        r.setRating(request.rating());
        r.setContent(request.content().trim());
        reviewRepository.save(r);
        return toResponse(r);
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponse> listByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponse> listMineSeller() {
        Long tenantId = requireSellerTenant();
        return reviewRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public ProductReviewResponse reply(Long reviewId, String reply) {
        Long tenantId = requireSellerTenant();
        ProductReview r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "评价不存在"));
        if (!tenantId.equals(r.getTenantId())) {
            throw new BusinessException(ErrorCode.TENANT_MISMATCH);
        }
        r.setSellerReply(reply == null ? "" : reply.trim());
        r.setRepliedAt(Instant.now());
        return toResponse(r);
    }

    private Long requireSellerTenant() {
        MeiyuePrincipal p = SecurityUtils.requirePrincipal();
        if (p.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED);
        }
        return p.getTenantId();
    }

    private ProductReviewResponse toResponse(ProductReview r) {
        return new ProductReviewResponse(
                r.getId(), r.getProductId(), r.getOrderId(), r.getOrderItemId(), r.getTenantId(),
                r.getRating(), r.getContent(), r.getSellerReply(),
                r.getCreatedAt() == null ? null : r.getCreatedAt().toString()
        );
    }
}
