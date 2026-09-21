package com.meiyuemall.catalog.service;

import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductFavorite;
import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.dto.FavoriteResponse;
import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.catalog.repo.ProductFavoriteRepository;
import com.meiyuemall.catalog.repo.ProductRepository;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 买家商品收藏（I22）。
 */
@Service
public class FavoriteService {

    private final ProductFavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final CatalogService catalogService;

    public FavoriteService(
            ProductFavoriteRepository favoriteRepository,
            ProductRepository productRepository,
            CatalogService catalogService
    ) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.catalogService = catalogService;
    }

    @Transactional
    public FavoriteResponse add(Long productId) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅可收藏已上架商品");
        }
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            ProductFavorite existing = favoriteRepository.findByUserIdAndProductId(userId, productId).orElseThrow();
            return toResponse(existing);
        }
        ProductFavorite fav = new ProductFavorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        favoriteRepository.save(fav);
        return toResponse(fav);
    }

    @Transactional
    public void remove(Long productId) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> listMine() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** 收藏商品详情列表（已下架的跳过） */
    @Transactional(readOnly = true)
    public List<ProductResponse> listMineProducts() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        List<ProductFavorite> favs = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (favs.isEmpty()) {
            return List.of();
        }
        List<Long> ids = favs.stream().map(ProductFavorite::getProductId).toList();
        Map<Long, Product> map = productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<ProductResponse> result = new ArrayList<>();
        for (ProductFavorite f : favs) {
            Product p = map.get(f.getProductId());
            if (p != null && p.getStatus() == ProductStatus.ON_SALE) {
                result.add(catalogService.toPublicResponse(p));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public boolean isFavorited(Long productId) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    private FavoriteResponse toResponse(ProductFavorite f) {
        return new FavoriteResponse(
                f.getId(),
                f.getProductId(),
                f.getCreatedAt() == null ? null : f.getCreatedAt().toString()
        );
    }
}
