package com.meiyuemall.catalog.repo;

import com.meiyuemall.catalog.domain.ProductFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 商品收藏仓储 */
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {

    List<ProductFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ProductFavorite> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}
