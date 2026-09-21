package com.meiyuemall.common.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * I7：拦截 {@link Audited} 方法，成功/失败各记一条审计。
 */
@Aspect
@Component
public class AuditedAspect {

    private final AuditLogService auditLogService;

    public AuditedAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(audited)")
    public Object around(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        try {
            Object result = pjp.proceed();
            String resourceId = extractId(result);
            auditLogService.record(
                    audited.action(),
                    audited.resourceType(),
                    resourceId,
                    "SUCCESS",
                    pjp.getSignature().toShortString()
            );
            return result;
        } catch (Throwable ex) {
            auditLogService.record(
                    audited.action(),
                    audited.resourceType(),
                    null,
                    "FAIL",
                    ex.getMessage()
            );
            throw ex;
        }
    }

    private static String extractId(Object result) {
        if (result == null) {
            return null;
        }
        try {
            var m = result.getClass().getMethod("id");
            Object id = m.invoke(result);
            return id == null ? null : String.valueOf(id);
        } catch (Exception ignored) {
            try {
                var m = result.getClass().getMethod("getId");
                Object id = m.invoke(result);
                return id == null ? null : String.valueOf(id);
            } catch (Exception ignored2) {
                return null;
            }
        }
    }
}
