package com.meiyuemall.trade.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.trade.dto.CreateReviewRequest;
import com.meiyuemall.trade.dto.ProductReviewResponse;
import com.meiyuemall.trade.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 商品评价 API */
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/buyer/reviews")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProductReviewResponse> create(@Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.ok(reviewService.create(request));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/products/{productId}/reviews")
    public ApiResponse<List<ProductReviewResponse>> byProduct(@PathVariable Long productId) {
        return ApiResponse.ok(reviewService.listByProduct(productId));
    }

    @GetMapping(SecurityConstants.API_PREFIX + "/seller/reviews")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<List<ProductReviewResponse>> sellerList() {
        return ApiResponse.ok(reviewService.listMineSeller());
    }

    @PostMapping(SecurityConstants.API_PREFIX + "/seller/reviews/{id}/reply")
    @PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
    public ApiResponse<ProductReviewResponse> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(reviewService.reply(id, body.getOrDefault("reply", "")));
    }
}
