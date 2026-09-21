package com.meiyuemall.trade.repo;

import com.meiyuemall.trade.domain.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);

    /** I30：公开列表仅未隐藏 */
    List<ProductReview> findByProductIdAndHiddenFalseOrderByCreatedAtDesc(Long productId);

    List<ProductReview> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<ProductReview> findAllByOrderByCreatedAtDesc();

    Optional<ProductReview> findByOrderItemId(Long orderItemId);

    boolean existsByOrderItemId(Long orderItemId);
}
