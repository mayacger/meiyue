package com.meiyuemall.trade.repo;

import com.meiyuemall.trade.domain.CouponClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponClaimRepository extends JpaRepository<CouponClaim, Long> {
    Optional<CouponClaim> findByIdAndBuyerUserId(Long id, Long buyerUserId);
    List<CouponClaim> findByBuyerUserIdOrderByClaimedAtDesc(Long buyerUserId);
    boolean existsByCouponIdAndBuyerUserId(Long couponId, Long buyerUserId);
}
