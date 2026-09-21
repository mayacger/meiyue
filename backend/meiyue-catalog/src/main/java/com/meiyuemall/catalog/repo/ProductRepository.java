package com.meiyuemall.catalog.repo;

import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTenantIdOrderByUpdatedAtDesc(Long tenantId);
    List<Product> findByTenantIdAndStatusOrderByUpdatedAtDesc(Long tenantId, ProductStatus status);
    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);
    List<Product> findByStatusOrderByUpdatedAtDesc(ProductStatus status);

    /** I30：同店在售（排除自身） */
    List<Product> findByTenantIdAndStatusAndIdNotOrderByUpdatedAtDesc(
            Long tenantId, ProductStatus status, Long id
    );

    /** I30：同类目在售（排除自身） */
    List<Product> findByCategoryIdAndStatusAndIdNotOrderByUpdatedAtDesc(
            Long categoryId, ProductStatus status, Long id
    );

    /**
     * I10 基础搜索：标题 / 副标题 / 类目名 LIKE；可选精确类目过滤。
     * 不上 OpenSearch；类目名通过 left join Category 匹配关键词。
     */
    @Query("""
            select distinct p from Product p
            left join com.meiyuemall.catalog.domain.Category c on c.id = p.categoryId
            where p.status = com.meiyuemall.catalog.domain.ProductStatus.ON_SALE
              and (:categoryId is null or p.categoryId = :categoryId)
              and (
                   :q is null or :q = ''
                   or lower(p.title) like lower(concat('%', cast(:q as string), '%'))
                   or lower(coalesce(p.subtitle, '')) like lower(concat('%', cast(:q as string), '%'))
                   or lower(coalesce(c.name, '')) like lower(concat('%', cast(:q as string), '%'))
              )
            order by p.updatedAt desc
            """)
    List<Product> searchOnSale(@Param("q") String q, @Param("categoryId") Long categoryId);
}
