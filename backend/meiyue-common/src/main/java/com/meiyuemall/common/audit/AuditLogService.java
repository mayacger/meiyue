package com.meiyuemall.common.audit;

import com.meiyuemall.common.observability.TraceIdFilter;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.common.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * I7 审计写入服务：落库 + 结构化日志；失败不影响主事务（独立 REQUIRES_NEW）。
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository repository;
    private final boolean enabled;

    public AuditLogService(
            AuditLogRepository repository,
            @Value("${meiyue.audit.enabled:true}") boolean enabled
    ) {
        this.repository = repository;
        this.enabled = enabled;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String resourceType, String resourceId, String outcome, String detail) {
        if (!enabled) {
            return;
        }
        try {
            AuditLog row = new AuditLog();
            MeiyuePrincipal principal = SecurityUtils.currentPrincipalOrNull();
            TenantContext.Holder holder = TenantContext.get();
            if (principal != null) {
                row.setActorUserId(principal.getUserId());
                row.setActorType(principal.getActorType().name());
                row.setTenantId(principal.getTenantId());
            } else {
                row.setActorType(holder.actorType().name());
                row.setTenantId(holder.tenantId());
                row.setActorUserId(holder.actorId());
            }
            row.setAction(action);
            row.setResourceType(resourceType);
            row.setResourceId(resourceId);
            row.setOutcome(outcome == null ? "SUCCESS" : outcome);
            row.setDetail(detail == null ? null : detail.substring(0, Math.min(detail.length(), 1024)));
            row.setTraceId(MDC.get(TraceIdFilter.MDC_TRACE));
            repository.save(row);
            log.info("audit action={} resource={}:{} outcome={} tenantId={} traceId={}",
                    action, resourceType, resourceId, outcome,
                    row.getTenantId(), row.getTraceId());
        } catch (Exception ex) {
            // 审计失败不得阻断业务
            log.warn("审计写入失败 action={}: {}", action, ex.getMessage());
        }
    }
}
