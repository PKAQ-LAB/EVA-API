package org.pkaq.core.auth.event;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.rbac.service.RoleResourceCacheService;
import org.pkaq.core.event.ModuleResourceChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 模块资源变更监听器。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class ModuleResourceChangedListener {

    private final RoleResourceCacheService roleResourceCacheService;

    /**
     * 业务事务提交后重建受影响角色的资源授权缓存。
     *
     * @param event 模块资源变更事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onModuleResourceChanged(ModuleResourceChangedEvent event) {
        roleResourceCacheService.rebuildRoleResources(event.getRoleIds());
    }
}
