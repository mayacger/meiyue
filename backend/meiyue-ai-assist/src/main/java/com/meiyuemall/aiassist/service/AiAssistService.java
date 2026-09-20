package com.meiyuemall.aiassist.service;

import com.meiyuemall.aiassist.config.AiProviderRegistry;
import com.meiyuemall.aiassist.domain.AssetType;
import com.meiyuemall.aiassist.domain.MediaAsset;
import com.meiyuemall.aiassist.domain.ModerationStatus;
import com.meiyuemall.aiassist.dto.DetailGenerateResponse;
import com.meiyuemall.aiassist.dto.GenerateDetailRequest;
import com.meiyuemall.aiassist.dto.GenerateImageRequest;
import com.meiyuemall.aiassist.dto.MediaAssetResponse;
import com.meiyuemall.aiassist.provider.AiDetailProvider;
import com.meiyuemall.aiassist.provider.AiImageProvider;
import com.meiyuemall.aiassist.repo.MediaAssetRepository;
import com.meiyuemall.aiassist.safety.ContentSafetyPort;
import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.catalog.service.CatalogService;
import com.meiyuemall.common.audit.Audited;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.redis.RedisAiTaskQueue;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * I8 AI 辅助：出图入库+审核、详情生成、挂接商品；失败降级人工。
 */
@Service
public class AiAssistService {

    private final AiProviderRegistry registry;
    private final MediaAssetRepository mediaAssetRepository;
    private final ContentSafetyPort contentSafetyPort;
    private final CatalogService catalogService;
    private final RedisAiTaskQueue aiTaskQueue;

    public AiAssistService(
            AiProviderRegistry registry,
            MediaAssetRepository mediaAssetRepository,
            ContentSafetyPort contentSafetyPort,
            CatalogService catalogService,
            RedisAiTaskQueue aiTaskQueue
    ) {
        this.registry = registry;
        this.mediaAssetRepository = mediaAssetRepository;
        this.contentSafetyPort = contentSafetyPort;
        this.catalogService = catalogService;
        this.aiTaskQueue = aiTaskQueue;
    }

    @Transactional
    @Audited(action = "AI_GENERATE_IMAGE", resourceType = "MediaAsset")
    public MediaAssetResponse generateImage(GenerateImageRequest request) {
        MeiyuePrincipal principal = requireSeller();
        AiImageProvider provider = registry.imageProvider();
        AiImageProvider.GenerateResult result = provider.generate(request.prompt());

        MediaAsset asset = new MediaAsset();
        asset.setTenantId(principal.getTenantId());
        asset.setAssetType(AssetType.IMAGE);
        asset.setCreatedBy(principal.getUserId());
        asset.setPrompt(request.prompt());

        if (!result.success()) {
            asset.setSource(result.source() == null ? "AI_FAIL" : result.source());
            asset.setUrl("mock://failed");
            asset.setFailReason(result.errorMessage());
            asset.setModerationStatus(ModerationStatus.REJECTED);
            asset.setModerationNote("生成失败，请人工上传");
            mediaAssetRepository.save(asset);
            throw new BusinessException(ErrorCode.AI_DEGRADED,
                    result.errorMessage() + "（可改用人工上传封面）");
        }

        asset.setSource(result.source());
        asset.setUrl(result.url());
        var safety = contentSafetyPort.reviewImageUrl(result.url(), request.prompt());
        if (safety.approved()) {
            asset.setModerationStatus(ModerationStatus.APPROVED);
            asset.setModerationNote(safety.note());
        } else {
            asset.setModerationStatus(ModerationStatus.REJECTED);
            asset.setModerationNote(safety.note());
        }
        mediaAssetRepository.save(asset);
        aiTaskQueue.enqueue("{\"type\":\"IMAGE\",\"assetId\":" + asset.getId() + ",\"tenantId\":" + asset.getTenantId() + "}");
        return toAssetResponse(asset, asset.getModerationStatus() != ModerationStatus.APPROVED);
    }

    @Transactional
    @Audited(action = "AI_GENERATE_DETAIL", resourceType = "MediaAsset")
    public DetailGenerateResponse generateDetail(GenerateDetailRequest request) {
        requireSeller();
        AiDetailProvider provider = registry.detailProvider();
        AiDetailProvider.DetailResult result = provider.generate(request.title(), request.hints());
        if (!result.success()) {
            return new DetailGenerateResponse(
                    false, registry.activeName(), null, null,
                    result.errorMessage() + "（可改用人工填写详情）", true);
        }
        var safety = contentSafetyPort.reviewText(result.detailHtml());
        if (!safety.approved()) {
            return new DetailGenerateResponse(
                    false, registry.activeName(), null, null,
                    "内容安全未通过: " + safety.note() + "（请人工改写）", true);
        }
        // 详情也可入库便于追溯
        MeiyuePrincipal principal = SecurityUtils.requirePrincipal();
        MediaAsset asset = new MediaAsset();
        asset.setTenantId(principal.getTenantId());
        asset.setAssetType(AssetType.DETAIL_HTML);
        asset.setSource(result.source());
        asset.setUrl("inline://detail/" + System.currentTimeMillis());
        asset.setPrompt(request.title() + " | " + (request.hints() == null ? "" : request.hints()));
        asset.setModerationStatus(ModerationStatus.APPROVED);
        asset.setModerationNote(safety.note());
        asset.setCreatedBy(principal.getUserId());
        mediaAssetRepository.save(asset);

        return new DetailGenerateResponse(
                true, registry.activeName(), result.titleSuggest(), result.detailHtml(), null, false);
    }

    /** 出图并挂到指定商品封面；失败抛 AI_DEGRADED */
    @Transactional
    public ProductResponse enrichProductCover(Long productId, GenerateImageRequest request) {
        MediaAssetResponse asset = generateImage(request);
        if (!"APPROVED".equals(asset.moderationStatus())) {
            throw new BusinessException(ErrorCode.MEDIA_NOT_APPROVED, "素材未通过审核，请人工上传封面");
        }
        return catalogService.attachCover(productId, asset.id(), asset.url());
    }

    /** 生成详情并写入商品 */
    @Transactional
    public ProductResponse enrichProductDetail(Long productId, GenerateDetailRequest request) {
        DetailGenerateResponse detail = generateDetail(request);
        if (!detail.success()) {
            throw new BusinessException(ErrorCode.AI_DEGRADED, detail.message());
        }
        return catalogService.applyDetailHtml(productId, detail.detailHtml());
    }

    /** 人工上传登记：写入素材库并标记 APPROVED（跳过 AI） */
    @Transactional
    @Audited(action = "MEDIA_MANUAL_UPLOAD", resourceType = "MediaAsset")
    public MediaAssetResponse registerManualImage(String url) {
        MeiyuePrincipal principal = requireSeller();
        if (url == null || url.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "封面 URL 不能为空");
        }
        var safety = contentSafetyPort.reviewImageUrl(url, url);
        MediaAsset asset = new MediaAsset();
        asset.setTenantId(principal.getTenantId());
        asset.setAssetType(AssetType.IMAGE);
        asset.setSource("MANUAL_UPLOAD");
        asset.setUrl(url.trim());
        asset.setCreatedBy(principal.getUserId());
        if (safety.approved()) {
            asset.setModerationStatus(ModerationStatus.APPROVED);
            asset.setModerationNote(safety.note());
        } else {
            asset.setModerationStatus(ModerationStatus.REJECTED);
            asset.setModerationNote(safety.note());
        }
        mediaAssetRepository.save(asset);
        return toAssetResponse(asset, true);
    }

    @Transactional(readOnly = true)
    public List<MediaAssetResponse> listMine() {
        Long tenantId = requireSeller().getTenantId();
        return mediaAssetRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(a -> toAssetResponse(a, false))
                .toList();
    }

    private MeiyuePrincipal requireSeller() {
        MeiyuePrincipal p = SecurityUtils.requirePrincipal();
        if (p.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED);
        }
        return p;
    }

    private MediaAssetResponse toAssetResponse(MediaAsset a, boolean allowManual) {
        boolean manual = allowManual || a.getModerationStatus() != ModerationStatus.APPROVED
                || a.getFailReason() != null;
        return new MediaAssetResponse(
                a.getId(), a.getTenantId(), a.getAssetType().name(), a.getSource(), a.getUrl(),
                a.getPrompt(), a.getModerationStatus().name(), a.getModerationNote(), a.getFailReason(),
                manual
        );
    }
}
