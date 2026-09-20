package com.meiyuemall.trade.repo;

import com.meiyuemall.trade.domain.Order;
import com.meiyuemall.trade.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    Optional<Order> findByIdAndBuyerUserId(Long id, Long buyerUserId);
    List<Order> findByBuyerUserIdOrderByCreatedAtDesc(Long buyerUserId);
    List<Order> findByStatusAndPayExpireAtBefore(OrderStatus status, Instant time);
}
