package org.pkaq.core.mybatis.tenant;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Supplier;

/**
 * 为平台管理员提供服务端可信的目标租户执行边界。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class TargetTenantExecutor {
    private final EvaConfig evaConfig;
    private final TenantSchemaRouter router;

    public <T> T execute(Long targetTenantId, Supplier<T> callback) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return callback.get();
        }
        if (!evaConfig.isPlatformMode() || !ThreadUserHelper.isAdmin()) {
            throw new SecurityException("只有平台管理员可以指定目标租户");
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("目标租户执行必须位于事务内");
        }
        String previous = router.routeCurrentTransaction(targetTenantId);
        try {
            return callback.get();
        } finally {
            router.restoreCurrentTransaction(previous);
            TenantContext.clear();
        }
    }
}
