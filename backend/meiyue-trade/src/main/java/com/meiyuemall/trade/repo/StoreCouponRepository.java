package com.meiyuemall.trade.repo;

import com.meiyuemall.trade.domain.StoreCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreCouponRepository extends JpaRepository<StoreCoupon, Long> {
    List<StoreCoupon> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<StoreCoupon> findByIdAndTenantId(Long id, Long tenantId);
    Optional<StoreCoupon> findByTenantIdAndCode(Long tenantId, String code);
}
