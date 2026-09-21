package com.meiyuemall.boot.service;

import com.meiyuemall.boot.dto.AuditLogResponse;
import com.meiyuemall.common.audit.AuditLog;
import com.meiyuemall.common.audit.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 审计日志只读查询（I24 Admin）。
 */
@Service
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;

    public AuditQueryService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 分页筛选。
     *
     * @param action       动作码模糊/精确（可空）
     * @param actorType    主体类型（可空）
     * @param resourceType 资源类型（可空）
     * @param tenantId     租户（可空）
     * @param page         页码从 0
     * @param size         页大小
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(
            String action,
            String actorType,
            String resourceType,
            Long tenantId,
            int page,
            int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (action != null && !action.isBlank()) {
                preds.add(cb.equal(root.get("action"), action.trim()));
            }
            if (actorType != null && !actorType.isBlank()) {
                preds.add(cb.equal(root.get("actorType"), actorType.trim().toUpperCase()));
            }
            if (resourceType != null && !resourceType.isBlank()) {
                preds.add(cb.equal(root.get("resourceType"), resourceType.trim()));
            }
            if (tenantId != null) {
                preds.add(cb.equal(root.get("tenantId"), tenantId));
            }
            return cb.and(preds.toArray(Predicate[]::new));
        };
        return auditLogRepository
                .findAll(spec, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getActorUserId(),
                a.getActorType(),
                a.getTenantId(),
                a.getAction(),
                a.getResourceType(),
                a.getResourceId(),
                a.getOutcome(),
                a.getDetail(),
                a.getTraceId(),
                a.getIp(),
                a.getCreatedAt() == null ? null : a.getCreatedAt().toString()
        );
    }
}
