package org.pkaq.core.auth.tenant;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.mybatis.tenant.TenantSchemaRouter;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * 在认证事务内按可信租户ID路由私有表。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class TenantAuthRoutingService {
    private final TransactionTemplate transactionTemplate;
    private final TenantSchemaRouter tenantSchemaRouter;
    private final EvaConfig evaConfig;

    public <T> T execute(Long tenantId, Supplier<T> action) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return action.get();
        }
        return transactionTemplate.execute(status -> {
            String previous = tenantSchemaRouter.routeCurrentTransaction(tenantId);
            try {
                return action.get();
            } finally {
                tenantSchemaRouter.restoreCurrentTransaction(previous);
                TenantContext.clear();
            }
        });
    }
}
