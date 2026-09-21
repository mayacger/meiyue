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

    /** 待发货：本店行且订单已支付未进入履约 */
    @Query("""
            select count(distinct o.id) from Order o join o.items i
            where i.tenantId = :tenantId and o.status = com.meiyuemall.trade.domain.OrderStatus.PAID
            """)
    long countPendingShipForSeller(@Param("tenantId") Long tenantId);

    /** 今日（paidAt 区间）本店行订单数 */
    @Query("""
            select count(distinct o.id) from Order o join o.items i
            where i.tenantId = :tenantId and o.paidAt is not null
              and o.paidAt >= :from and o.paidAt < :to
            """)
    long countPaidTodayForSeller(
            @Param("tenantId") Long tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    /** 今日本店销售额（订单行小计之和） */
    @Query("""
            select coalesce(sum(i.lineTotalCents), 0) from OrderItem i
            join i.order o
            where i.tenantId = :tenantId and o.paidAt is not null
              and o.paidAt >= :from and o.paidAt < :to
            """)
    long sumSalesCentsTodayForSeller(
            @Param("tenantId") Long tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    /**
     * I22：本店已支付订单（paidAt 非空），用于 CSV 导出。
     */
    @Query("""
            select distinct o from Order o join o.items i
            where i.tenantId = :tenantId and o.paidAt is not null
            order by o.paidAt desc
            """)
    List<Order> findPaidForSeller(@Param("tenantId") Long tenantId);

    /** I22：全站已支付订单（Admin 导出） */
    List<Order> findByPaidAtIsNotNullOrderByPaidAtDesc();
}
