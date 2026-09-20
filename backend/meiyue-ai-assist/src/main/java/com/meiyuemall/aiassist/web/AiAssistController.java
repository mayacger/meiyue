package com.meiyuemall.aiassist.web;

import com.meiyuemall.aiassist.dto.AiVideoTaskResponse;
import com.meiyuemall.aiassist.dto.DetailGenerateResponse;
import com.meiyuemall.aiassist.dto.GenerateDetailRequest;
import com.meiyuemall.aiassist.dto.GenerateImageRequest;
import com.meiyuemall.aiassist.dto.GenerateVideoRequest;
import com.meiyuemall.aiassist.dto.MediaAssetResponse;
import com.meiyuemall.aiassist.service.AiAssistService;
import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * I8/I11 商家 AI 辅助 API。
 * <p>支持推广视频 MOCK（非直播）。失败响应含 allowManualUpload / AI_DEGRADED。</p>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/seller/ai")
@PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
public class AiAssistController {

    private final AiAssistService aiAssistService;

    public AiAssistController(AiAssistService aiAssistService) {
        this.aiAssistService = aiAssistService;
    }

    @PostMapping("/images")
    public ApiResponse<MediaAssetResponse> generateImage(@Valid @RequestBody GenerateImageRequest request) {
        return ApiResponse.ok(aiAssistService.generateImage(request));
    }

    @PostMapping("/details")
    public ApiResponse<DetailGenerateResponse> generateDetail(@Valid @RequestBody GenerateDetailRequest request) {
        return ApiResponse.ok(aiAssistService.generateDetail(request));
    }

    @PostMapping("/products/{productId}/cover")
    public ApiResponse<ProductResponse> enrichCover(
            @PathVariable Long productId,
            @Valid @RequestBody GenerateImageRequest request
    ) {
        return ApiResponse.ok(aiAssistService.enrichProductCover(productId, request));
    }

    @PostMapping("/products/{productId}/detail")
    public ApiResponse<ProductResponse> enrichDetail(
            @PathVariable Long productId,
            @Valid @RequestBody GenerateDetailRequest request
    ) {
        return ApiResponse.ok(aiAssistService.enrichProductDetail(productId, request));
    }

    @PostMapping("/manual-images")
    public ApiResponse<MediaAssetResponse> manualUpload(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(aiAssistService.registerManualImage(body.get("url")));
    }

    @GetMapping("/assets")
    public ApiResponse<List<MediaAssetResponse>> assets() {
        return ApiResponse.ok(aiAssistService.listMine());
    }

    /** I11：提交推广视频异步任务（MOCK） */
    @PostMapping("/videos")
    public ApiResponse<AiVideoTaskResponse> submitVideo(@Valid @RequestBody GenerateVideoRequest request) {
        return ApiResponse.ok(aiAssistService.submitVideo(request));
    }

    @GetMapping("/videos")
    public ApiResponse<List<AiVideoTaskResponse>> listVideos() {
        return ApiResponse.ok(aiAssistService.listVideoTasks());
    }

    @PostMapping("/products/{productId}/promo-video")
    public ApiResponse<ProductResponse> attachPromoVideo(
            @PathVariable Long productId,
            @RequestBody Map<String, Long> body
    ) {
        return ApiResponse.ok(aiAssistService.attachPromoVideo(productId, body.get("assetId")));
    }
}
