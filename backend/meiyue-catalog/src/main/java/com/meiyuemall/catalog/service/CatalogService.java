package com.meiyuemall.catalog.service;

import com.meiyuemall.catalog.domain.Category;
import com.meiyuemall.catalog.domain.CategoryStatus;
import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductSku;
import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.dto.CategoryResponse;
import com.meiyuemall.catalog.dto.ProductResponse;
import com.meiyuemall.catalog.dto.ProductUpsertRequest;
import com.meiyuemall.catalog.dto.SkuResponse;
import com.meiyuemall.catalog.repo.CategoryRepository;
import com.meiyuemall.catalog.repo.ProductRepository;
import com.meiyuemall.common.audit.Audited;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品目录服务：商家 CRUD + 上下架；买家只读已上架。
 * <p>所有写操作强制使用主体 tenantId，忽略客户端伪造。</p>
 */
@Service
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CatalogService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findByStatusOrderBySortOrderAsc(CategoryStatus.ENABLED).stream()
                .map(c -> new CategoryResponse(c.getId(), c.getParentId(), c.getName(), c.getSortOrder()))
                .toList();
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
                product.getStatus().name(),
                skus,
                product.getUpdatedAt()
        );
    }
}
