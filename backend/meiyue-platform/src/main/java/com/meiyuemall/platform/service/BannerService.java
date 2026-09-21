package com.meiyuemall.platform.service;

import com.meiyuemall.common.audit.Audited;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.platform.domain.PlatformBanner;
import com.meiyuemall.platform.dto.BannerResponse;
import com.meiyuemall.platform.dto.BannerUpsertRequest;
import com.meiyuemall.platform.repo.PlatformBannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 平台首页 Banner（I28）：公开列表 + Admin CRUD。
 * <p>禁止直播组件；仅图文跳转运营位。</p>
 */
@Service
public class BannerService {

    private final PlatformBannerRepository bannerRepository;

    public BannerService(PlatformBannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    /** 买家端：当前可展示 Banner */
    @Transactional(readOnly = true)
    public List<BannerResponse> listActive() {
        return bannerRepository.findActive(Instant.now()).stream().map(this::toResponse).toList();
    }

    /** Admin：全部 */
    @Transactional(readOnly = true)
    public List<BannerResponse> listAll() {
        return bannerRepository.findAllByOrderBySortOrderDescIdDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    @Audited(action = "BANNER_CREATE", resourceType = "PlatformBanner")
    public BannerResponse create(BannerUpsertRequest req) {
        PlatformBanner b = new PlatformBanner();
        apply(b, req);
        bannerRepository.save(b);
        return toResponse(b);
    }

    @Transactional
    @Audited(action = "BANNER_UPDATE", resourceType = "PlatformBanner")
    public BannerResponse update(Long id, BannerUpsertRequest req) {
        PlatformBanner b = bannerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Banner 不存在"));
        apply(b, req);
        return toResponse(b);
    }

    @Transactional
    @Audited(action = "BANNER_DELETE", resourceType = "PlatformBanner")
    public void delete(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Banner 不存在");
        }
        bannerRepository.deleteById(id);
    }

    private void apply(PlatformBanner b, BannerUpsertRequest req) {
        b.setTitle(req.title().trim());
        b.setImageUrl(req.imageUrl().trim());
        b.setLinkUrl(req.linkUrl() == null || req.linkUrl().isBlank() ? null : req.linkUrl().trim());
        b.setSortOrder(req.sortOrder() == null ? 0 : req.sortOrder());
        b.setEnabled(req.enabled() == null || req.enabled());
        b.setStartAt(parseInstant(req.startAt()));
        b.setEndAt(parseInstant(req.endAt()));
    }

    private Instant parseInstant(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(raw.trim());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "时间格式须为 ISO-8601 Instant");
        }
    }

    private BannerResponse toResponse(PlatformBanner b) {
        return new BannerResponse(
                b.getId(),
                b.getTitle(),
                b.getImageUrl(),
                b.getLinkUrl(),
                b.getSortOrder(),
                b.isEnabled(),
                b.getStartAt() == null ? null : b.getStartAt().toString(),
                b.getEndAt() == null ? null : b.getEndAt().toString()
        );
    }
}
