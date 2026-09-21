package com.meiyuemall.tenant.service;

import com.meiyuemall.common.audit.Audited;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.identity.domain.RoleCode;
import com.meiyuemall.identity.service.UserRoleService;
import com.meiyuemall.tenant.domain.MemberRole;
import com.meiyuemall.tenant.domain.OnboardingApplication;
import com.meiyuemall.tenant.domain.OnboardingStatus;
import com.meiyuemall.tenant.domain.SellerMember;
import com.meiyuemall.tenant.domain.Store;
import com.meiyuemall.tenant.domain.StoreStatus;
import com.meiyuemall.tenant.domain.Tenant;
import com.meiyuemall.tenant.domain.TenantStatus;
import com.meiyuemall.tenant.dto.OnboardingApplicationResponse;
import com.meiyuemall.tenant.dto.OnboardingApplyRequest;
import com.meiyuemall.tenant.dto.ReviewOnboardingRequest;
import com.meiyuemall.tenant.dto.StoreResponse;
import com.meiyuemall.tenant.dto.StoreUpdateRequest;
import com.meiyuemall.tenant.repo.OnboardingApplicationRepository;
import com.meiyuemall.tenant.repo.SellerMemberRepository;
import com.meiyuemall.tenant.repo.StoreRepository;
import com.meiyuemall.tenant.repo.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 入驻申请 → 平台审核 → 开店（1 租户 : 1 店铺）闭环。
 * <p>
 * 关系：
 * Application(PENDING) --approve--&gt; Tenant + Store + SellerMember(OWNER) + Role(SELLER_OWNER)
 * </p>
 */
@Service
public class OnboardingService {

    private final OnboardingApplicationRepository applicationRepository;
    private final TenantRepository tenantRepository;
    private final StoreRepository storeRepository;
    private final SellerMemberRepository sellerMemberRepository;
    private final UserRoleService userRoleService;

    public OnboardingService(
            OnboardingApplicationRepository applicationRepository,
            TenantRepository tenantRepository,
            StoreRepository storeRepository,
            SellerMemberRepository sellerMemberRepository,
            UserRoleService userRoleService
    ) {
        this.applicationRepository = applicationRepository;
        this.tenantRepository = tenantRepository;
        this.storeRepository = storeRepository;
        this.sellerMemberRepository = sellerMemberRepository;
        this.userRoleService = userRoleService;
    }

    /**
     * 商家提交入驻申请。同一用户不可重复处于 PENDING；已有店铺成员也不可再申请。
     */
    @Transactional
    public OnboardingApplicationResponse apply(OnboardingApplyRequest request) {
        MeiyuePrincipal principal = SecurityUtils.requirePrincipal();
        Long userId = principal.getUserId();

        if (sellerMemberRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.APPLICATION_EXISTS, "您已拥有店铺，无需重复入驻");
        }
        if (applicationRepository.existsByApplicantUserIdAndStatus(userId, OnboardingStatus.PENDING)) {
            throw new BusinessException(ErrorCode.APPLICATION_EXISTS, "已有待审核申请");
        }
        String slug = request.shopSlug().trim().toLowerCase();
        if (applicationRepository.existsByShopSlug(slug) || storeRepository.existsBySlug(slug)) {
            throw new BusinessException(ErrorCode.CONFLICT, "店铺 slug 已被占用");
        }

        OnboardingApplication app = new OnboardingApplication();
        app.setApplicantUserId(userId);
        app.setShopName(request.shopName().trim());
        app.setShopSlug(slug);
        app.setContactName(request.contactName().trim());
        app.setContactPhone(request.contactPhone().trim());
        app.setStatus(OnboardingStatus.PENDING);
        applicationRepository.save(app);
        return toResponse(app);
    }

    @Transactional(readOnly = true)
    public OnboardingApplicationResponse myLatestApplication() {
        MeiyuePrincipal principal = SecurityUtils.requirePrincipal();
        return applicationRepository.findFirstByApplicantUserIdOrderByCreatedAtDesc(principal.getUserId())
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "暂无入驻申请"));
    }

    @Transactional(readOnly = true)
    public List<OnboardingApplicationResponse> listPending() {
        return applicationRepository.findByStatusOrderByCreatedAtAsc(OnboardingStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 平台通过：创建租户、店铺、店主成员，并授予 SELLER_OWNER。
     */
    @Transactional
    public OnboardingApplicationResponse approve(Long applicationId, ReviewOnboardingRequest request) {
        MeiyuePrincipal admin = SecurityUtils.requirePrincipal();
        OnboardingApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申请不存在"));
        if (app.getStatus() != OnboardingStatus.PENDING) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_PENDING);
        }
        if (storeRepository.existsBySlug(app.getShopSlug())) {
            throw new BusinessException(ErrorCode.CONFLICT, "店铺 slug 已被占用");
        }
        if (sellerMemberRepository.existsByUserId(app.getApplicantUserId())) {
            throw new BusinessException(ErrorCode.APPLICATION_EXISTS, "申请人已是店铺成员");
        }

        Tenant tenant = new Tenant();
        tenant.setName(app.getShopName());
        tenant.setStatus(TenantStatus.ACTIVE);
        tenantRepository.save(tenant);

        Store store = new Store();
        store.setTenantId(tenant.getId());
        store.setName(app.getShopName());
        store.setSlug(app.getShopSlug());
        store.setStatus(StoreStatus.OPEN);
        storeRepository.save(store);

        SellerMember member = new SellerMember();
        member.setTenantId(tenant.getId());
        member.setUserId(app.getApplicantUserId());
        member.setMemberRole(MemberRole.OWNER);
        sellerMemberRepository.save(member);

        userRoleService.grantRole(app.getApplicantUserId(), RoleCode.SELLER_OWNER);

        app.setStatus(OnboardingStatus.APPROVED);
        app.setTenantId(tenant.getId());
        app.setReviewedBy(admin.getUserId());
        app.setReviewedAt(Instant.now());
        app.setReviewNote(request == null ? null : request.reviewNote());
        applicationRepository.save(app);
        return toResponse(app);
    }

    @Transactional
    public OnboardingApplicationResponse reject(Long applicationId, ReviewOnboardingRequest request) {
        MeiyuePrincipal admin = SecurityUtils.requirePrincipal();
        OnboardingApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申请不存在"));
        if (app.getStatus() != OnboardingStatus.PENDING) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_PENDING);
        }
        app.setStatus(OnboardingStatus.REJECTED);
        app.setReviewedBy(admin.getUserId());
        app.setReviewedAt(Instant.now());
        app.setReviewNote(request == null ? null : request.reviewNote());
        applicationRepository.save(app);
        return toResponse(app);
    }

    @Transactional(readOnly = true)
    public StoreResponse myStore() {
        MeiyuePrincipal principal = SecurityUtils.requirePrincipal();
        if (principal.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED, "尚未开店或未完成入驻审核");
        }
        Store store = storeRepository.findByTenantId(principal.getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "店铺不存在"));
        // 防越权：主体 tenant 必须与店铺一致（双重保险）
        if (!store.getTenantId().equals(principal.getTenantId())) {
            throw new BusinessException(ErrorCode.TENANT_MISMATCH);
        }
        return toStoreResponse(store);
    }

    private OnboardingApplicationResponse toResponse(OnboardingApplication app) {
        return new OnboardingApplicationResponse(
                app.getId(),
                app.getApplicantUserId(),
                app.getShopName(),
                app.getShopSlug(),
                app.getContactName(),
                app.getContactPhone(),
                app.getStatus().name(),
                app.getReviewNote(),
                app.getTenantId(),
                app.getCreatedAt(),
                app.getReviewedAt()
        );
    }

    private StoreResponse toStoreResponse(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getTenantId(),
                store.getName(),
                store.getSlug(),
                store.getDescription(),
                store.getLogoUrl(),
                store.getStatus().name(),
                store.getCreatedAt(),
                store.getFreightCents(),
                store.getFreeShippingThresholdCents()
        );
    }

    /**
     * I22/I31：更新本店名称/简介/Logo/运费模板。
     */
    @Transactional
    @Audited(action = "STORE_UPDATE", resourceType = "Store")
    public StoreResponse updateMyStore(StoreUpdateRequest request) {
        MeiyuePrincipal principal = SecurityUtils.requirePrincipal();
        if (principal.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED, "尚未开店或未完成入驻审核");
        }
        Store store = storeRepository.findByTenantId(principal.getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "店铺不存在"));
        if (request.name() != null && !request.name().isBlank()) {
            store.setName(request.name().trim());
        }
        if (request.description() != null) {
            store.setDescription(request.description().isBlank() ? null : request.description().trim());
        }
        if (request.logoUrl() != null) {
            store.setLogoUrl(request.logoUrl().isBlank() ? null : request.logoUrl().trim());
        }
        if (request.freightCents() != null) {
            store.setFreightCents(Math.max(0, request.freightCents()));
        }
        if (request.freeShippingThresholdCents() != null) {
            // 负数约定：清空包邮门槛
            if (request.freeShippingThresholdCents() < 0) {
                store.setFreeShippingThresholdCents(null);
            } else {
                store.setFreeShippingThresholdCents(request.freeShippingThresholdCents());
            }
        }
        return toStoreResponse(store);
    }

    /** 公开：按租户查店 */
    @Transactional(readOnly = true)
    public StoreResponse getPublicByTenant(Long tenantId) {
        Store store = storeRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "店铺不存在"));
        return toStoreResponse(store);
    }

    /** 公开：按 slug 查店 */
    @Transactional(readOnly = true)
    public StoreResponse getPublicBySlug(String slug) {
        Store store = storeRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "店铺不存在"));
        return toStoreResponse(store);
    }
}
