package com.meiyuemall.catalog.service;

import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductBrowseHistory;
import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.catalog.repo.ProductBrowseHistoryRepository;
import com.meiyuemall.catalog.repo.ProductRepository;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 买家浏览足迹（I30）：记录 / 列表 / 清空。
 */
@Service
public class BrowseHistoryService {

    private static final int MAX_KEEP = 100;

    private final ProductBrowseHistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final CatalogService catalogService;

    public BrowseHistoryService(
            ProductBrowseHistoryRepository historyRepository,
            ProductRepository productRepository,
            CatalogService catalogService
    ) {
        this.historyRepository = historyRepository;
        this.productRepository = productRepository;
        this.catalogService = catalogService;
    }

    /** 记录浏览（upsert）；商品须存在 */
    @Transactional
    public void record(Long productId) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        ProductBrowseHistory row = historyRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> {
                    ProductBrowseHistory h = new ProductBrowseHistory();
                    h.setUserId(userId);
                    h.setProductId(productId);
                    return h;
                });
        row.setBrowsedAt(Instant.now());
        historyRepository.save(row);
        // 简单裁剪：超出上限删最旧
        List<ProductBrowseHistory> all = historyRepository.findByUserIdOrderByBrowsedAtDesc(userId);
        if (all.size() > MAX_KEEP) {
            historyRepository.deleteAll(all.subList(MAX_KEEP, all.size()));
        }
    }

    /** 足迹商品列表（已下架跳过） */
    @Transactional(readOnly = true)
    public List<ProductResponse> listMineProducts() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        List<ProductBrowseHistory> rows = historyRepository.findByUserIdOrderByBrowsedAtDesc(userId);
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, Product> map = productRepository.findAllById(
                rows.stream().map(ProductBrowseHistory::getProductId).toList()
        ).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        List<ProductResponse> result = new ArrayList<>();
        for (ProductBrowseHistory h : rows) {
            Product p = map.get(h.getProductId());
            if (p != null && p.getStatus() == ProductStatus.ON_SALE) {
                result.add(catalogService.toPublicResponse(p));
            }
        }
        return result;
    }

    @Transactional
    public void clearMine() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        historyRepository.deleteByUserId(userId);
    }
}
