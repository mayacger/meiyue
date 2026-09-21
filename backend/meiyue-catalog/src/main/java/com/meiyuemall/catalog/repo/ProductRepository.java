package com.meiyuemall.catalog.repo;

import com.meiyuemall.catalog.domain.Product;
import com.meiyuemall.catalog.domain.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    /** 本店未删除商品（默认列表） */
    List<Product> findByTenantIdAndDeletedAtIsNullOrderByUpdatedAtDesc(Long tenantId);

    List<Product> findByTenantIdAndStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(
            Long tenantId, ProductStatus status
    );

    /** 本店回收站 */
    List<Product> findByTenantIdAndDeletedAtIsNotNullOrderByUpdatedAtDesc(Long tenantId);

    /** 平台回收站治理 */
    List<Product> findByDeletedAtIsNotNullOrderByUpdatedAtDesc();

    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Product> findByIdAndTenantIdAndDeletedAtIsNull(Long id, Long tenantId);

    List<Product> findByStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(ProductStatus status);

    /** I30：同店在售（排除自身、已删） */
    List<Product> findByTenantIdAndStatusAndIdNotAndDeletedAtIsNullOrderByUpdatedAtDesc(
            Long tenantId, ProductStatus status, Long id
    );

    /** I30：同类目在售（排除自身、已删） */
    List<Product> findByCategoryIdAndStatusAndIdNotAndDeletedAtIsNullOrderByUpdatedAtDesc(
            Long categoryId, ProductStatus status, Long id
    );

    /**
     * I10 基础搜索：标题 / 副标题 / 类目名 LIKE；排除软删。
     */
    @Query("""
            select distinct p from Product p
            left join com.meiyuemall.catalog.domain.Category c on c.id = p.categoryId
            where p.status = com.meiyuemall.catalog.domain.ProductStatus.ON_SALE
              and p.deletedAt is null
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
