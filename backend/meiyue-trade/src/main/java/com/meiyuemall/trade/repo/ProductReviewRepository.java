package com.meiyuemall.trade.repo;

import com.meiyuemall.trade.domain.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);
    List<ProductReview> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<ProductReview> findByOrderItemId(Long orderItemId);
    boolean existsByOrderItemId(Long orderItemId);
}
