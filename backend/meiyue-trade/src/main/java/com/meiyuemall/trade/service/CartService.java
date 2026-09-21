package com.meiyuemall.trade.service;

import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductSku;
import com.meiyuemall.catalog.domain.ProductStatus;
import com.meiyuemall.catalog.repo.ProductRepository;
import com.meiyuemall.catalog.repo.ProductSkuRepository;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.trade.domain.CartItem;
import com.meiyuemall.trade.dto.CartAddRequest;
import com.meiyuemall.trade.dto.CartItemResponse;
import com.meiyuemall.trade.repo.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨店购物车：行级带 tenant_id，结算时逻辑拆单到同一订单多店行。
 */
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductSkuRepository productSkuRepository,
            ProductRepository productRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productSkuRepository = productSkuRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartItemResponse add(CartAddRequest request) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        ProductSku sku = productSkuRepository.findById(request.skuId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "SKU 不存在"));
        Product product = sku.getProduct();
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品未上架");
        }
        CartItem item = cartItemRepository.findByBuyerUserIdAndSkuId(buyerId, sku.getId())
                .orElseGet(() -> {
                    CartItem c = new CartItem();
                    c.setBuyerUserId(buyerId);
                    c.setSkuId(sku.getId());
                    c.setProductId(product.getId());
                    c.setTenantId(product.getTenantId());
                    c.setQuantity(0);
                    return c;
                });
        item.setQuantity(item.getQuantity() + request.quantity());
        cartItemRepository.save(item);
        return toResponse(item, product, sku);
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> listMine() {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        List<CartItemResponse> result = new ArrayList<>();
        for (CartItem item : cartItemRepository.findByBuyerUserIdOrderByUpdatedAtDesc(buyerId)) {
            ProductSku sku = productSkuRepository.findById(item.getSkuId()).orElse(null);
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (sku == null || product == null) {
                continue;
            }
            result.add(toResponse(item, product, sku));
        }
        return result;
    }

    @Transactional
    public void remove(Long cartItemId) {
        Long buyerId = SecurityUtils.requirePrincipal().getUserId();
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "购物车行不存在"));
        if (!item.getBuyerUserId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        cartItemRepository.delete(item);
    }

    private CartItemResponse toResponse(CartItem item, Product product, ProductSku sku) {
        long line = sku.getPriceCents() * item.getQuantity();
        return new CartItemResponse(
                item.getId(),
                item.getTenantId(),
                item.getProductId(),
                item.getSkuId(),
                product.getTitle(),
                sku.getSkuCode(),
                sku.getPriceCents(),
                item.getQuantity(),
                line
        );
    }
}
