package com.meiyuemall.aiassist.repo;

import com.meiyuemall.aiassist.domain.AiVideoTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiVideoTaskRepository extends JpaRepository<AiVideoTask, Long> {
    List<AiVideoTask> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<AiVideoTask> findByIdAndTenantId(Long id, Long tenantId);
}
