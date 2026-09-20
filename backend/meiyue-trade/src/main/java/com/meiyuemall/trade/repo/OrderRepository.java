package com.meiyuemall.trade.repo;

import com.meiyuemall.trade.domain.Order;
import com.meiyuemall.trade.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    Optional<Order> findByIdAndBuyerUserId(Long id, Long buyerUserId);
    List<Order> findByBuyerUserIdOrderByCreatedAtDesc(Long buyerUserId);
    List<Order> findByStatusAndPayExpireAtBefore(OrderStatus status, Instant time);

    @Query("""
            select distinct o from Order o join o.items i
            where i.tenantId = :tenantId and o.status in :statuses
            order by o.createdAt desc
            """)
    List<Order> findDistinctForSeller(
            @Param("tenantId") Long tenantId,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}
