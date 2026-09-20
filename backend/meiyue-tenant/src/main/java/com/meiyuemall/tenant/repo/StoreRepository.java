package com.meiyuemall.tenant.repo;

import com.meiyuemall.tenant.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByTenantId(Long tenantId);

    Optional<Store> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
