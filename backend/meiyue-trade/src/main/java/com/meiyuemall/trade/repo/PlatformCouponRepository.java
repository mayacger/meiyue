package com.meiyuemall.trade.repo;

import com.meiyuemall.trade.domain.PlatformCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformCouponRepository extends JpaRepository<PlatformCoupon, Long> {
    Optional<PlatformCoupon> findByCode(String code);
    List<PlatformCoupon> findByStatusOrderByCreatedAtDesc(String status);
}
