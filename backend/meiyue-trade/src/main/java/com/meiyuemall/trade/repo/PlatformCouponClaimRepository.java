package com.meiyuemall.trade.repo;

import com.meiyuemall.trade.domain.PlatformCouponClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformCouponClaimRepository extends JpaRepository<PlatformCouponClaim, Long> {
    Optional<PlatformCouponClaim> findByIdAndBuyerUserId(Long id, Long buyerUserId);
    List<PlatformCouponClaim> findByBuyerUserIdOrderByClaimedAtDesc(Long buyerUserId);
    boolean existsByCouponIdAndBuyerUserId(Long couponId, Long buyerUserId);
}
