package com.meiyuemall.aiassist.repo;

import com.meiyuemall.aiassist.domain.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    List<MediaAsset> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    Optional<MediaAsset> findByIdAndTenantId(Long id, Long tenantId);
}
