package com.meiyuemall.trade.repo;

import com.meiyuemall.trade.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByBuyerUserIdOrderByUpdatedAtDesc(Long buyerUserId);
    Optional<CartItem> findByBuyerUserIdAndSkuId(Long buyerUserId, Long skuId);
    void deleteByBuyerUserIdAndSkuIdIn(Long buyerUserId, java.util.Collection<Long> skuIds);
}
