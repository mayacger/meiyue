package com.meiyuemall.catalog.repo;

import com.meiyuemall.catalog.domain.Category;
import com.meiyuemall.catalog.domain.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByStatusOrderBySortOrderAsc(CategoryStatus status);

    List<Category> findAllByOrderBySortOrderAscIdAsc();
}
