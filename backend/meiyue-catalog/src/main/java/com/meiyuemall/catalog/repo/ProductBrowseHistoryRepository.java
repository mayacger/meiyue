package com.meiyuemall.catalog.repo;

import com.meiyuemall.catalog.domain.ProductBrowseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductBrowseHistoryRepository extends JpaRepository<ProductBrowseHistory, Long> {

    List<ProductBrowseHistory> findByUserIdOrderByBrowsedAtDesc(Long userId);

    Optional<ProductBrowseHistory> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);

    long countByUserId(Long userId);
}
