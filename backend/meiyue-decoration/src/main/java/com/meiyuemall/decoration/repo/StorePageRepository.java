package com.meiyuemall.decoration.repo;

import com.meiyuemall.decoration.domain.StorePage;
import com.meiyuemall.decoration.domain.StorePageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StorePageRepository extends JpaRepository<StorePage, Long> {
    Optional<StorePage> findByTenantIdAndStatus(Long tenantId, StorePageStatus status);
}
