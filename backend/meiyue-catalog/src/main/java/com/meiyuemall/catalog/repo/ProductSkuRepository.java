package com.meiyuemall.catalog.repo;

import com.meiyuemall.catalog.domain.ProductSku;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {

    /** 下单预占时悲观锁行，防超卖 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProductSku s where s.id = :id")
    Optional<ProductSku> findByIdForUpdate(@Param("id") Long id);

    Optional<ProductSku> findByIdAndTenantId(Long id, Long tenantId);

    @Query("""
            select s from ProductSku s join fetch s.product
            where s.id = :id and s.tenantId = :tenantId
            """)
    Optional<ProductSku> findByIdAndTenantIdWithProduct(
            @Param("id") Long id,
            @Param("tenantId") Long tenantId
    );

    /** 库存页：带商品标题 */
    @Query("""
            select s from ProductSku s join fetch s.product p
            where s.tenantId = :tenantId
            order by p.id asc, s.id asc
            """)
    List<ProductSku> findByTenantIdWithProduct(@Param("tenantId") Long tenantId);
}
