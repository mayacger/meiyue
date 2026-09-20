package com.meiyuemall.catalog.repo;

import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTenantIdOrderByUpdatedAtDesc(Long tenantId);
    List<Product> findByTenantIdAndStatusOrderByUpdatedAtDesc(Long tenantId, ProductStatus status);
    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);
    List<Product> findByStatusOrderByUpdatedAtDesc(ProductStatus status);
}
