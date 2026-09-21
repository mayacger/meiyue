package com.meiyuemall.catalog.service;

import com.meiyuemall.catalog.domain.Category;
import com.meiyuemall.catalog.domain.CategoryStatus;
import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductSku;
import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.dto.BatchProductStatusRequest;
import com.meiyuemall.catalog.dto.CategoryResponse;
import com.meiyuemall.catalog.dto.CategoryUpsertRequest;
import com.meiyuemall.catalog.dto.InventorySkuResponse;
import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.catalog.dto.ProductUpsertRequest;
import com.meiyuemall.catalog.dto.SkuResponse;
import com.meiyuemall.catalog.dto.StockAdjustRequest;
import com.meiyuemall.catalog.repo.CategoryRepository;
import com.meiyuemall.catalog.repo.ProductRepository;
import com.meiyuemall.catalog.repo.ProductSkuRepository;
import com.meiyuemall.common.audit.Audited;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品目录服务：商家 CRUD + 上下架；买家只读已上架。
 * <p>所有写操作强制使用主体 tenantId，忽略客户端伪造。</p>
 */
@Service
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;

    public CatalogService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ProductSkuRepository productSkuRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productSkuRepository = productSkuRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findByStatusOrderBySortOrderAsc(CategoryStatus.ENABLED).stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    /** 平台管理：含禁用类目 */
    @Transactional(readOnly = true)
    public List<CategoryResponse> listAllCategoriesForAdmin() {
        return categoryRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Transactional
    @Audited(action = "CATEGORY_CREATE", resourceType = "Category")
    public CategoryResponse createCategory(CategoryUpsertRequest request) {
        validateParent(request.parentId());
        Category c = new Category();
        c.setParentId(request.parentId());
        c.setName(request.name().trim());
        c.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        c.setStatus(CategoryStatus.ENABLED);
        categoryRepository.save(c);
        return toCategoryResponse(c);
    }

    @Transactional
    @Audited(action = "CATEGORY_UPDATE", resourceType = "Category")
    public CategoryResponse updateCategory(Long id, CategoryUpsertRequest request) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "类目不存在"));
        if (request.parentId() != null && request.parentId().equals(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "父类目不能是自身");
        }
        validateParent(request.parentId());
        c.setParentId(request.parentId());
        c.setName(request.name().trim());
        if (request.sortOrder() != null) {
            c.setSortOrder(request.sortOrder());
        }
        return toCategoryResponse(c);
    }

    @Transactional
    @Audited(action = "CATEGORY_STATUS", resourceType = "Category")
    public CategoryResponse changeCategoryStatus(Long id, CategoryStatus status) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "类目不存在"));
        c.setStatus(status);
        return toCategoryResponse(c);
    }

    private void validateParent(Long parentId) {
        if (parentId == null) {
            return;
        }
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "父类目不存在"));
        if (parent.getStatus() != CategoryStatus.ENABLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "父类目已禁用");
        }
    }

    private CategoryResponse toCategoryResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getParentId(), c.getName(), c.getSortOrder(), c.getStatus());
    }

    @Transactional
    @Audited(action = "PRODUCT_CREATE", resourceType = "Product")
    public ProductResponse create(ProductUpsertRequest request) {
        Long tenantId = requireSellerTenant();
        Product product = new Product();
        product.setTenantId(tenantId);
        apply(product, request, tenantId);
        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    @Audited(action = "PRODUCT_UPDATE", resourceType = "Product")
    public ProductResponse update(Long productId, ProductUpsertRequest request) {
        Long tenantId = requireSellerTenant();
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        product.getSkus().clear();
        apply(product, request, tenantId);
        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    @Audited(action = "PRODUCT_STATUS", resourceType = "Product")
    public ProductResponse changeStatus(Long productId, ProductStatus status) {
        Long tenantId = requireSellerTenant();
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        if (status == ProductStatus.ON_SALE && product.getSkus().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无 SKU 不可上架");
        }
        product.setStatus(status);
        return toResponse(product);
    }

    /**
     * I25：批量上下架（本店商品；单条失败则整批回滚）。
     */
    @Transactional
    @Audited(action = "PRODUCT_BATCH_STATUS", resourceType = "Product")
    public List<ProductResponse> batchChangeStatus(BatchProductStatusRequest request) {
        Long tenantId = requireSellerTenant();
        ProductStatus status = ProductStatus.valueOf(request.status());
        List<ProductResponse> result = new ArrayList<>();
        for (Long productId : request.productIds()) {
            Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在: " + productId));
            if (status == ProductStatus.ON_SALE && product.getSkus().isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "商品 " + productId + " 无 SKU 不可上架");
            }
            product.setStatus(status);
            result.add(toResponse(product));
        }
        return result;
    }

    /**
     * I8：将已审核素材挂到商品封面（AI 或人工流程共用）。
     */
    @Transactional
    @Audited(action = "PRODUCT_ATTACH_COVER", resourceType = "Product")
    public ProductResponse attachCover(Long productId, Long assetId, String coverUrl) {
        Long tenantId = requireSellerTenant();
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        product.setCoverAssetId(assetId);
        product.setCoverImageUrl(coverUrl);
        return toResponse(product);
    }

    /**
     * I8：写入 AI/人工详情 HTML。
     */
    @Transactional
    @Audited(action = "PRODUCT_APPLY_DETAIL", resourceType = "Product")
    public ProductResponse applyDetailHtml(Long productId, String detailHtml) {
        Long tenantId = requireSellerTenant();
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        product.setDetailHtml(detailHtml);
        return toResponse(product);
    }

    /**
     * I11：挂接推广视频（商家主体校验）。
     */
    @Transactional
    @Audited(action = "PRODUCT_ATTACH_PROMO_VIDEO", resourceType = "Product")
    public ProductResponse attachPromoVideo(Long productId, Long assetId, String videoUrl) {
        Long tenantId = requireSellerTenant();
        return attachPromoVideoInternal(tenantId, productId, assetId, videoUrl);
    }

    /**
     * 内部挂接（异步任务完成时调用，已校验租户）。
     */
    @Transactional
    public ProductResponse attachPromoVideoInternal(Long tenantId, Long productId, Long assetId, String videoUrl) {
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        product.setPromoVideoAssetId(assetId);
        product.setPromoVideoUrl(videoUrl);
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listMine() {
        Long tenantId = requireSellerTenant();
        return productRepository.findByTenantIdOrderByUpdatedAtDesc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listOnSale() {
        return productRepository.findByStatusOrderByUpdatedAtDesc(ProductStatus.ON_SALE).stream()
                .map(this::toResponse)
                .toList();
    }

    /** I10：标题/副标题关键词 + 可选类目（DB LIKE） */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchOnSale(String q, Long categoryId) {
        String keyword = (q == null || q.isBlank()) ? null : q.trim();
        return productRepository.searchOnSale(keyword, categoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listOnSaleByTenant(Long tenantId) {
        return productRepository.findByTenantIdAndStatusOrderByUpdatedAtDesc(tenantId, ProductStatus.ON_SALE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getOnSale(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品未上架");
        }
        return toResponse(product);
    }

    /**
     * I20：本店全部 SKU 库存列表。
     */
    @Transactional(readOnly = true)
    public List<InventorySkuResponse> listInventory() {
        Long tenantId = requireSellerTenant();
        return productSkuRepository.findByTenantIdWithProduct(tenantId).stream()
                .map(s -> new InventorySkuResponse(
                        s.getId(),
                        s.getProduct().getId(),
                        s.getProduct().getTitle(),
                        s.getSkuCode(),
                        s.getSpecText(),
                        s.getPriceCents(),
                        s.getStockQty(),
                        s.getProduct().getStatus().name()
                ))
                .toList();
    }

    /**
     * I25：库存预警列表（默认阈值 5，含 0）。
     *
     * @param threshold 库存 ≤ threshold 的 SKU
     */
    @Transactional(readOnly = true)
    public List<InventorySkuResponse> listLowStock(int threshold) {
        int th = Math.max(threshold, 0);
        return listInventory().stream()
                .filter(s -> s.stockQty() <= th)
                .toList();
    }

    /**
     * I20：将 SKU 库存设为绝对值。
     */
    @Transactional
    @Audited(action = "SKU_STOCK_ADJUST", resourceType = "ProductSku")
    public InventorySkuResponse adjustStock(Long skuId, StockAdjustRequest request) {
        Long tenantId = requireSellerTenant();
        ProductSku sku = productSkuRepository.findByIdAndTenantIdWithProduct(skuId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "SKU 不存在"));
        sku.setStockQty(request.stockQty());
        return new InventorySkuResponse(
                sku.getId(),
                sku.getProduct().getId(),
                sku.getProduct().getTitle(),
                sku.getSkuCode(),
                sku.getSpecText(),
                sku.getPriceCents(),
                sku.getStockQty(),
                sku.getProduct().getStatus().name()
        );
    }

    private void apply(Product product, ProductUpsertRequest request, Long tenantId) {
        product.setCategoryId(request.categoryId());
        product.setTitle(request.title().trim());
        product.setSubtitle(request.subtitle());
        product.setDetailHtml(request.detailHtml());
        // I8：封面可人工 URL，或由 AI 挂接写入 coverAssetId + coverImageUrl
        if (request.coverImageUrl() != null) {
            product.setCoverImageUrl(request.coverImageUrl().isBlank() ? null : request.coverImageUrl().trim());
        }
        for (ProductUpsertRequest.SkuRequest skuReq : request.skus()) {
            ProductSku sku = new ProductSku();
            sku.setTenantId(tenantId);
            sku.setProduct(product);
            sku.setSkuCode(skuReq.skuCode().trim());
            sku.setSpecText(skuReq.specText());
            sku.setPriceCents(skuReq.priceCents());
            sku.setStockQty(skuReq.stockQty());
            product.getSkus().add(sku);
        }
    }

    private Long requireSellerTenant() {
        MeiyuePrincipal principal = SecurityUtils.requirePrincipal();
        if (principal.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED, "需要已开店的商家身份");
        }
        return principal.getTenantId();
    }

    /**
     * I22：公开商品视图（收藏列表等复用；不含商家私有字段差异）。
     */
    public ProductResponse toPublicResponse(Product product) {
        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        List<SkuResponse> skus = product.getSkus().stream()
                .map(s -> new SkuResponse(s.getId(), s.getSkuCode(), s.getSpecText(), s.getPriceCents(), s.getStockQty()))
                .toList();
        return new ProductResponse(
                product.getId(),
                product.getTenantId(),
                product.getCategoryId(),
                product.getTitle(),
                product.getSubtitle(),
                product.getDetailHtml(),
                product.getCoverImageUrl(),
                product.getCoverAssetId(),
                product.getPromoVideoUrl(),
                product.getPromoVideoAssetId(),
                product.getStatus().name(),
                skus,
                product.getUpdatedAt()
        );
    }
}
