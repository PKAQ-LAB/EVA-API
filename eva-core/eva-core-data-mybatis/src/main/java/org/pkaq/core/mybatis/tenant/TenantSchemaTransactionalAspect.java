package org.pkaq.core.mybatis.tenant;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
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
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class TenantSchemaTransactionalAspect {
    private final TenantSchemaRouter router;

    @Around("@annotation(tenantSchema)")
    public Object route(ProceedingJoinPoint joinPoint, TenantSchema tenantSchema) throws Throwable {
        String previous = router.routeCurrentTransaction();
        try {
            return joinPoint.proceed();
        } finally {
            router.restoreCurrentTransaction(previous);
            TenantContext.clear();
        }
    }
}
