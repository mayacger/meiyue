package com.meiyuemall.boot.service;

import com.meiyuemall.boot.dto.AdminDashboardResponse;
import com.meiyuemall.boot.dto.DashboardSeriesResponse;
import com.meiyuemall.boot.dto.SellerDashboardResponse;
import com.meiyuemall.aftersale.domain.AftersaleStatus;
import com.meiyuemall.aftersale.repo.AftersaleRepository;
import com.meiyuemall.catalog.domain.CategoryStatus;
import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.repo.CategoryRepository;
import com.meiyuemall.catalog.repo.ProductRepository;
import com.meiyuemall.catalog.repo.ProductSkuRepository;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.identity.domain.RoleCode;
import com.meiyuemall.identity.domain.UserAccount;
import com.meiyuemall.identity.repo.UserAccountRepository;
import com.meiyuemall.tenant.domain.OnboardingStatus;
import com.meiyuemall.tenant.repo.OnboardingApplicationRepository;
import com.meiyuemall.trade.repo.OrderRepository;
import com.meiyuemall.trade.repo.PlatformCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * I20：跨模块经营/运营概览聚合（boot 组装层）。
 */
@Service
public class DashboardAggregateService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final OrderRepository orderRepository;
    private final AftersaleRepository aftersaleRepository;
    private final ProductSkuRepository productSkuRepository;
    private final OnboardingApplicationRepository onboardingApplicationRepository;
    private final UserAccountRepository userAccountRepository;
    private final CategoryRepository categoryRepository;
    private final PlatformCouponRepository platformCouponRepository;
    private final ProductRepository productRepository;

    public DashboardAggregateService(
            OrderRepository orderRepository,
            AftersaleRepository aftersaleRepository,
            ProductSkuRepository productSkuRepository,
            OnboardingApplicationRepository onboardingApplicationRepository,
            UserAccountRepository userAccountRepository,
            CategoryRepository categoryRepository,
            PlatformCouponRepository platformCouponRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.aftersaleRepository = aftersaleRepository;
        this.productSkuRepository = productSkuRepository;
        this.onboardingApplicationRepository = onboardingApplicationRepository;
        this.userAccountRepository = userAccountRepository;
        this.categoryRepository = categoryRepository;
        this.platformCouponRepository = platformCouponRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public SellerDashboardResponse sellerStats() {
        Long tenantId = requireSellerTenant();
        LocalDate today = LocalDate.now(ZONE);
        Instant from = today.atStartOfDay(ZONE).toInstant();
        Instant to = today.plusDays(1).atStartOfDay(ZONE).toInstant();

        long pendingShip = orderRepository.countPendingShipForSeller(tenantId);
        long pendingAs = aftersaleRepository.countByTenantIdAndStatusIn(
                tenantId,
                EnumSet.of(AftersaleStatus.APPLIED, AftersaleStatus.REVIEWING)
        );
        long todayOrders = orderRepository.countPaidTodayForSeller(tenantId, from, to);
        long todaySales = orderRepository.sumSalesCentsTodayForSeller(tenantId, from, to);
        long lowStock = productSkuRepository.findByTenantIdWithProduct(tenantId).stream()
                .filter(s -> s.getStockQty() <= 5)
                .count();

        return new SellerDashboardResponse(pendingShip, pendingAs, todayOrders, todaySales, lowStock);
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse adminStats() {
        long pendingOnboarding = onboardingApplicationRepository
                .findByStatusOrderByCreatedAtAsc(OnboardingStatus.PENDING)
                .size();
        List<UserAccount> users = userAccountRepository.findAll();
        long buyers = users.stream().filter(u -> u.getRoles().contains(RoleCode.BUYER)).count();
        long sellers = users.stream().filter(u -> u.getRoles().contains(RoleCode.SELLER_OWNER)).count();
        long categories = categoryRepository.findByStatusOrderBySortOrderAsc(CategoryStatus.ENABLED).size();
        long coupons = platformCouponRepository.count();
        long onSale = productRepository.findByStatusOrderByUpdatedAtDesc(ProductStatus.ON_SALE).size();
        return new AdminDashboardResponse(pendingOnboarding, buyers, sellers, categories, coupons, onSale);
    }

    /**
     * I27：近 days 日商家销售额/订单序列（含今日）。
     */
    @Transactional(readOnly = true)
    public DashboardSeriesResponse sellerSeries(int days) {
        Long tenantId = requireSellerTenant();
        return buildSeries(days, (from, to) -> new long[]{
                orderRepository.countPaidTodayForSeller(tenantId, from, to),
                orderRepository.sumSalesCentsTodayForSeller(tenantId, from, to)
        });
    }

    /**
     * I27：近 days 日平台销售额/订单序列。
     */
    @Transactional(readOnly = true)
    public DashboardSeriesResponse adminSeries(int days) {
        return buildSeries(days, (from, to) -> new long[]{
                orderRepository.countPaidBetween(from, to),
                orderRepository.sumSalesCentsBetween(from, to)
        });
    }

    private DashboardSeriesResponse buildSeries(int days, DayAggregator agg) {
        int n = Math.min(Math.max(days, 1), 30);
        LocalDate today = LocalDate.now(ZONE);
        List<DashboardSeriesResponse.DayPoint> points = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Instant from = d.atStartOfDay(ZONE).toInstant();
            Instant to = d.plusDays(1).atStartOfDay(ZONE).toInstant();
            long[] v = agg.aggregate(from, to);
            points.add(new DashboardSeriesResponse.DayPoint(d.toString(), v[0], v[1]));
        }
        return new DashboardSeriesResponse(points);
    }

    @FunctionalInterface
    private interface DayAggregator {
        long[] aggregate(Instant from, Instant to);
    }

    private Long requireSellerTenant() {
        MeiyuePrincipal p = SecurityUtils.requirePrincipal();
        if (p.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED, "需要已开店的商家身份");
        }
        return p.getTenantId();
    }
}
