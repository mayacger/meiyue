package com.meiyuemall.trade.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.trade.domain.PlatformCoupon;
import com.meiyuemall.trade.domain.PlatformCouponClaim;
import com.meiyuemall.trade.dto.CreatePlatformCouponRequest;
import com.meiyuemall.trade.dto.PlatformCouponClaimResponse;
import com.meiyuemall.trade.dto.PlatformCouponResponse;
import com.meiyuemall.trade.repo.PlatformCouponClaimRepository;
import com.meiyuemall.trade.repo.PlatformCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 平台券：平台发放、买家领取、下单抵扣。
 * <p>与店券规则见 {@link CouponStackingRules}。</p>
 */
@Service
public class PlatformCouponService {

    private final PlatformCouponRepository couponRepository;
    private final PlatformCouponClaimRepository claimRepository;

    public PlatformCouponService(
            PlatformCouponRepository couponRepository,
            PlatformCouponClaimRepository claimRepository
    ) {
        this.couponRepository = couponRepository;
        this.claimRepository = claimRepository;
    }

    @Transactional
    public PlatformCouponResponse create(CreatePlatformCouponRequest request) {
        couponRepository.findByCode(request.code().trim().toUpperCase()).ifPresent(c -> {
            throw new BusinessException(ErrorCode.CONFLICT, "平台券码已存在");
        });
        PlatformCoupon c = new PlatformCoupon();
        c.setCode(request.code().trim().toUpperCase());
        c.setTitle(request.title().trim());
        c.setDiscountCents(request.discountCents());
        c.setMinSpendCents(request.minSpendCents());
        c.setTotalQuota(request.totalQuota());
        c.setClaimedCount(0);
        c.setStatus("ACTIVE");
        couponRepository.save(c);
        return toCoupon(c);
    }

    @Transactional(readOnly = true)
    public List<PlatformCouponResponse> listActive() {
        return couponRepository.findByStatusOrderByCreatedAtDesc("ACTIVE").stream()
                .map(this::toCoupon).toList();
    }

    @Transactional
    public PlatformCouponClaimResponse claim(Long couponId) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        PlatformCoupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "平台券不存在"));
        if (!"ACTIVE".equals(coupon.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "平台券不可领取");
        }
        if (coupon.getEndsAt() != null && coupon.getEndsAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "平台券已过期");
        }
        if (coupon.getTotalQuota() > 0 && coupon.getClaimedCount() >= coupon.getTotalQuota()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "平台券已领完");
        }
        if (claimRepository.existsByCouponIdAndBuyerUserId(couponId, buyerId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "已领取过该平台券");
        }
        coupon.setClaimedCount(coupon.getClaimedCount() + 1);
        PlatformCouponClaim claim = new PlatformCouponClaim();
        claim.setCouponId(coupon.getId());
        claim.setBuyerUserId(buyerId);
        claim.setStatus("CLAIMED");
        claimRepository.save(claim);
        return toClaim(claim);
    }

    @Transactional(readOnly = true)
    public List<PlatformCouponClaimResponse> listMine() {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        return claimRepository.findByBuyerUserIdOrderByClaimedAtDesc(buyerId).stream()
                .map(this::toClaim).toList();
    }

    /**
     * 整单门槛校验后抵扣；返回抵扣金额。
     */
    @Transactional
    public long applyClaimToOrder(Long claimId, Long buyerId, long orderSubtotalCents, Long orderId) {
        PlatformCouponClaim claim = claimRepository.findByIdAndBuyerUserId(claimId, buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "平台领券记录不存在"));
        if (!"CLAIMED".equals(claim.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "平台券不可用");
        }
        PlatformCoupon coupon = couponRepository.findById(claim.getCouponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "平台券不存在"));
        if (orderSubtotalCents < coupon.getMinSpendCents()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未达到平台券使用门槛");
        }
        long discount = Math.min(coupon.getDiscountCents(), orderSubtotalCents);
        claim.setStatus("USED");
        claim.setOrderId(orderId);
        claim.setDiscountAppliedCents(discount);
        claim.setUsedAt(Instant.now());
        return discount;
    }

    private PlatformCouponResponse toCoupon(PlatformCoupon c) {
        return new PlatformCouponResponse(
                c.getId(), c.getCode(), c.getTitle(), c.getDiscountCents(),
                c.getMinSpendCents(), c.getTotalQuota(), c.getClaimedCount(), c.getStatus()
        );
    }

    private PlatformCouponClaimResponse toClaim(PlatformCouponClaim c) {
        return new PlatformCouponClaimResponse(
                c.getId(), c.getCouponId(), c.getStatus(), c.getOrderId(), c.getDiscountAppliedCents()
        );
    }
}
