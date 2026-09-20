package com.meiyuemall.aiassist.service;

import com.meiyuemall.aiassist.config.AiProviderRegistry;
import com.meiyuemall.aiassist.domain.AiVideoTask;
import com.meiyuemall.aiassist.domain.AssetType;
import com.meiyuemall.aiassist.domain.MediaAsset;
import com.meiyuemall.aiassist.domain.ModerationStatus;
import com.meiyuemall.aiassist.dto.AiVideoTaskResponse;
import com.meiyuemall.aiassist.dto.DetailGenerateResponse;
import com.meiyuemall.aiassist.dto.GenerateDetailRequest;
import com.meiyuemall.aiassist.dto.GenerateImageRequest;
import com.meiyuemall.aiassist.dto.GenerateVideoRequest;
import com.meiyuemall.aiassist.dto.MediaAssetResponse;
import com.meiyuemall.aiassist.provider.AiDetailProvider;
import com.meiyuemall.aiassist.provider.AiImageProvider;
import com.meiyuemall.aiassist.provider.AiVideoProvider;
import com.meiyuemall.aiassist.repo.AiVideoTaskRepository;
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
 * I8/I11 AI 辅助：出图、详情、推广视频（MOCK 异步）；挂接商品；失败降级人工。
 * <p>硬约束：推广视频 ≠ 直播。</p>
 */
@Service
public class AiAssistService {

    private final AiProviderRegistry registry;
    private final MediaAssetRepository mediaAssetRepository;
    private final AiVideoTaskRepository videoTaskRepository;
    private final ContentSafetyPort contentSafetyPort;
    private final CatalogService catalogService;
    private final RedisAiTaskQueue aiTaskQueue;

    public AiAssistService(
            AiProviderRegistry registry,
            MediaAssetRepository mediaAssetRepository,
            AiVideoTaskRepository videoTaskRepository,
            ContentSafetyPort contentSafetyPort,
            CatalogService catalogService,
            RedisAiTaskQueue aiTaskQueue
    ) {
        this.registry = registry;
        this.mediaAssetRepository = mediaAssetRepository;
        this.videoTaskRepository = videoTaskRepository;
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

    /**
     * I11：提交推广视频异步任务（默认 MOCK）。
     * 入队后由 {@code AiTaskQueueJob} 消费生成占位 URL 并可选挂商品。
     */
    @Transactional
    @Audited(action = "AI_SUBMIT_VIDEO", resourceType = "AiVideoTask")
    public AiVideoTaskResponse submitVideo(GenerateVideoRequest request) {
        MeiyuePrincipal principal = requireSeller();
        String prompt = request.prompt().trim();
        if (prompt.toUpperCase().contains("LIVE") || prompt.contains("直播")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "禁止生成直播类内容；仅支持推广短视频");
        }
        AiVideoTask task = new AiVideoTask();
        task.setTenantId(principal.getTenantId());
        task.setProductId(request.productId());
        task.setStatus("PENDING");
        task.setPrompt(prompt);
        task.setCreatedBy(principal.getUserId());
        videoTaskRepository.save(task);
        aiTaskQueue.enqueue("{\"type\":\"VIDEO\",\"taskId\":" + task.getId()
                + ",\"tenantId\":" + task.getTenantId() + "}");
        return toVideoResponse(task, null);
    }

    @Transactional(readOnly = true)
    public List<AiVideoTaskResponse> listVideoTasks() {
        Long tenantId = requireSeller().getTenantId();
        return videoTaskRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(t -> {
                    String url = null;
                    if (t.getResultAssetId() != null) {
                        url = mediaAssetRepository.findById(t.getResultAssetId()).map(MediaAsset::getUrl).orElse(null);
                    }
                    return toVideoResponse(t, url);
                }).toList();
    }

    /**
     * 消费 VIDEO 异步任务：MOCK 生成素材 → APPROVED → 可选挂商品。
     * @return true 若已处理
     */
    @Transactional
    public boolean processVideoTask(Long taskId) {
        AiVideoTask task = videoTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return false;
        }
        if ("SUCCEEDED".equals(task.getStatus()) || "FAILED".equals(task.getStatus())) {
            return false;
        }
        task.setStatus("RUNNING");
        AiVideoProvider provider = registry.videoProvider();
        AiVideoProvider.VideoResult result = provider.generate(task.getPrompt());
        if (!result.success()) {
            task.setStatus("FAILED");
            task.setFailReason(result.errorMessage());
            return true;
        }
        MediaAsset asset = new MediaAsset();
        asset.setTenantId(task.getTenantId());
        asset.setAssetType(AssetType.VIDEO);
        asset.setSource(result.source());
        asset.setUrl(result.url());
        asset.setPrompt(task.getPrompt());
        asset.setModerationStatus(ModerationStatus.APPROVED);
        asset.setModerationNote("MOCK 推广视频自动通过（非直播）");
        asset.setCreatedBy(task.getCreatedBy());
        mediaAssetRepository.save(asset);

        task.setResultAssetId(asset.getId());
        task.setStatus("SUCCEEDED");
        if (task.getProductId() != null) {
            catalogService.attachPromoVideoInternal(task.getTenantId(), task.getProductId(), asset.getId(), asset.getUrl());
        }
        return true;
    }

    /** 将已有 VIDEO 素材挂到商品 */
    @Transactional
    public ProductResponse attachPromoVideo(Long productId, Long assetId) {
        MeiyuePrincipal principal = requireSeller();
        MediaAsset asset = mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "素材不存在"));
        if (!principal.getTenantId().equals(asset.getTenantId())) {
            throw new BusinessException(ErrorCode.TENANT_MISMATCH);
        }
        if (asset.getAssetType() != AssetType.VIDEO) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅 VIDEO 素材可挂推广视频");
        }
        if (asset.getModerationStatus() != ModerationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.MEDIA_NOT_APPROVED);
        }
        return catalogService.attachPromoVideo(productId, asset.getId(), asset.getUrl());
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

    private AiVideoTaskResponse toVideoResponse(AiVideoTask t, String resultUrl) {
        return new AiVideoTaskResponse(
                t.getId(), t.getTenantId(), t.getProductId(), t.getStatus(), t.getPrompt(),
                t.getResultAssetId(), resultUrl, t.getFailReason(),
                t.getCreatedAt() == null ? null : t.getCreatedAt().toString()
        );
    }
}
