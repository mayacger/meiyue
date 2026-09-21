package com.meiyuemall.trade.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.trade.domain.CouponClaim;
import com.meiyuemall.trade.domain.StoreCoupon;
import com.meiyuemall.trade.dto.CouponClaimResponse;
import com.meiyuemall.trade.dto.CreateStoreCouponRequest;
import com.meiyuemall.trade.dto.StoreCouponResponse;
import com.meiyuemall.trade.repo.CouponClaimRepository;
import com.meiyuemall.trade.repo.StoreCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 店券：发券 / 领券 / 下单抵扣（平台券后置）。
 */
@Service
public class CouponService {

    private final StoreCouponRepository couponRepository;
    private final CouponClaimRepository claimRepository;

    public CouponService(StoreCouponRepository couponRepository, CouponClaimRepository claimRepository) {
        this.couponRepository = couponRepository;
        this.claimRepository = claimRepository;
    }

    @Transactional
    public StoreCouponResponse create(CreateStoreCouponRequest request) {
        Long tenantId = requireSellerTenant();
        couponRepository.findByTenantIdAndCode(tenantId, request.code().trim().toUpperCase())
                .ifPresent(c -> { throw new BusinessException(ErrorCode.CONFLICT, "券码已存在"); });
        StoreCoupon c = new StoreCoupon();
        c.setTenantId(tenantId);
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
    public List<StoreCouponResponse> listMineSeller() {
        return couponRepository.findByTenantIdOrderByCreatedAtDesc(requireSellerTenant())
                .stream().map(this::toCoupon).toList();
    }

    /** 公开：某店可领券列表 */
    @Transactional(readOnly = true)
    public List<StoreCouponResponse> listActiveByTenant(Long tenantId) {
        return couponRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .filter(c -> "ACTIVE".equals(c.getStatus()))
                .map(this::toCoupon)
                .toList();
    }

    @Transactional
    public CouponClaimResponse claim(Long couponId) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        StoreCoupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));
        if (!"ACTIVE".equals(coupon.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券不可领取");
        }
        if (coupon.getEndsAt() != null && coupon.getEndsAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券已过期");
        }
        if (coupon.getTotalQuota() > 0 && coupon.getClaimedCount() >= coupon.getTotalQuota()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券已领完");
        }
        if (claimRepository.existsByCouponIdAndBuyerUserId(couponId, buyerId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "已领取过该券");
        }
        coupon.setClaimedCount(coupon.getClaimedCount() + 1);
        CouponClaim claim = new CouponClaim();
        claim.setCouponId(coupon.getId());
        claim.setTenantId(coupon.getTenantId());
        claim.setBuyerUserId(buyerId);
        claim.setStatus("CLAIMED");
        claimRepository.save(claim);
        return toClaim(claim);
    }

    @Transactional(readOnly = true)
    public List<CouponClaimResponse> listMineBuyer() {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        return claimRepository.findByBuyerUserIdOrderByClaimedAtDesc(buyerId).stream()
                .map(this::toClaim).toList();
    }

    /**
     * 下单抵扣：校验领券归属与门槛，返回抵扣金额并标记 USED。
     * 仅当订单含该店商品行时可用（由调用方保证租户匹配）。
     */
    @Transactional
    public long applyClaimToOrder(Long claimId, Long buyerId, Long tenantId, long orderSubtotalCents, Long orderId) {
        CouponClaim claim = claimRepository.findByIdAndBuyerUserId(claimId, buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "领券记录不存在"));
        if (!"CLAIMED".equals(claim.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券不可用");
        }
        if (!tenantId.equals(claim.getTenantId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券不适用于本店商品");
        }
        StoreCoupon coupon = couponRepository.findById(claim.getCouponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));
        if (orderSubtotalCents < coupon.getMinSpendCents()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未达到优惠券使用门槛");
        }
        long discount = Math.min(coupon.getDiscountCents(), orderSubtotalCents);
        claim.setStatus("USED");
        claim.setOrderId(orderId);
        claim.setDiscountAppliedCents(discount);
        claim.setUsedAt(Instant.now());
        return discount;
    }

    private Long requireSellerTenant() {
        MeiyuePrincipal p = SecurityUtils.requirePrincipal();
        if (p.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED);
        }
        return p.getTenantId();
    }

    private StoreCouponResponse toCoupon(StoreCoupon c) {
        return new StoreCouponResponse(
                c.getId(), c.getTenantId(), c.getCode(), c.getTitle(),
                c.getDiscountCents(), c.getMinSpendCents(), c.getTotalQuota(),
                c.getClaimedCount(), c.getStatus()
        );
    }

    private CouponClaimResponse toClaim(CouponClaim c) {
        return new CouponClaimResponse(
                c.getId(), c.getCouponId(), c.getTenantId(), c.getStatus(),
                c.getOrderId(), c.getDiscountAppliedCents()
        );
    }
}
