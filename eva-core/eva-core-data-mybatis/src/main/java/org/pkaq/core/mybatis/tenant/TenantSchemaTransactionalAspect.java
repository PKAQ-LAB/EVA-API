package org.pkaq.core.mybatis.tenant;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.pkaq.core.tenant.TenantContext;

/**
 * 在事务业务方法进入时应用可信租户 schema。
 *
 * @author PKAQ
 */
@Aspect
@Component
@Order(TenantRoutingOrder.SCHEMA_ROUTING)
@RequiredArgsConstructor
public class TenantSchemaTransactionalAspect {
    private final TenantSchemaRouter router;

    @Around("@annotation(tenantSchema)")
    public Object route(ProceedingJoinPoint joinPoint, TenantSchema tenantSchema) throws Throwable {
        Long previousTenantId = TenantContext.tenantId();
        String previousSchemaName = TenantContext.schemaName();
        String previous = router.routeCurrentTransaction();
        try {
            return joinPoint.proceed();
        } finally {
            router.restoreCurrentTransaction(previous);
            restoreTenantContext(previousTenantId, previousSchemaName);
        }
    }

    private void restoreTenantContext(Long tenantId, String schemaName) {
        if (tenantId == null || schemaName == null) {
            TenantContext.clear();
            return;
        }
        TenantContext.bind(tenantId, schemaName);
    }
}
