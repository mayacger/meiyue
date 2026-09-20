package com.meiyuemall.catalog.repo;

import com.meiyuemall.catalog.domain.ProductSku;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {

    /** 下单预占时悲观锁行，防超卖 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProductSku s where s.id = :id")
    Optional<ProductSku> findByIdForUpdate(@Param("id") Long id);
}
