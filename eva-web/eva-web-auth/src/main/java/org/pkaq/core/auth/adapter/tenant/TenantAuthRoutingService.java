package org.pkaq.core.auth.adapter.tenant;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
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
 * @date 2026-10-08
 */
@Component
@RequiredArgsConstructor
public class TenantAuthRoutingService implements ITenantAuthRouter {
    private final TransactionTemplate transactionTemplate;
    private final TenantSchemaRouter tenantSchemaRouter;
    private final EvaConfig evaConfig;

    /** 在租户事务执行认证查询，并恢复调用前的路由和线程上下文。 */
    @Override
    public <T> T execute(Long tenantId, Supplier<T> action) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return action.get();
        }
        return transactionTemplate.execute(status -> {
            Long previousTenantId = TenantContext.tenantId();
            String previousSchema = TenantContext.schemaName();
            String previousSearchPath = null;
            try {
                previousSearchPath = tenantSchemaRouter.routeCurrentTransaction(tenantId);
                return action.get();
            } finally {
                try {
                    if (previousSearchPath != null) {
                        tenantSchemaRouter.restoreCurrentTransaction(previousSearchPath);
                    }
                } finally {
                    // 嵌套查询必须恢复外层身份；路由或恢复失败也不得泄漏线程上下文。
                    if (previousTenantId == null && previousSchema == null) {
                        TenantContext.clear();
                    } else {
                        TenantContext.bind(previousTenantId, previousSchema);
                    }
                }
            }
        });
    }
}
