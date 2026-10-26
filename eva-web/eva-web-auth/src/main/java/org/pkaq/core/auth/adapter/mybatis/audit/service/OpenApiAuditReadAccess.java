package org.pkaq.core.auth.adapter.mybatis.audit.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.authorization.service.ResourceAuthorizationService;
import org.pkaq.core.auth.spi.IOpenApiAuditAdministratorQuery;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mybatis.log.OpenApiAuditQueryScope;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

/**
 * 集中审计读取的常驻管理员守卫及可信schema路由。
 * @author PKAQ
 * @date 2026-10-24
 */
@Service
@RequiredArgsConstructor
public class OpenApiAuditReadAccess {
    private final EvaConfig evaConfig;
    private final IOpenApiAuditAdministratorQuery administratorQuery;
    private final ResourceAuthorizationService resourceAuthorizationService;
    private final CoreSchemaExecutor coreSchemaExecutor;

    /**
     * 在只读事务中核验有效管理员、资源资格后执行同源日志读取。
     * @param path 原资源鉴权路径
     * @param action 同步读取动作
     * @param <T> 结果类型
     * @return 读取结果
     */
    @Transactional(readOnly = true)
    public <T> T read(String path, Supplier<T> action) {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        boolean standalone = evaConfig.isStandaloneMode() && !evaConfig.getTenant().isSchemaMode();
        boolean platform = evaConfig.isPlatformMode() && evaConfig.getTenant().isSchemaMode();
        if (user == null || user.getUserId() <= 0L || user.getTenantId() != 0L || !standalone && !platform) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        if (standalone && (TenantContext.tenantId() != null && TenantContext.tenantId() != 0L
                || TenantContext.schemaName() != null
                && !evaConfig.getTenant().getStandaloneSchema().equals(TenantContext.schemaName()))) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        if (platform) {
            return coreSchemaExecutor.execute(jdbc -> readInSchema(user, path, action,
                    evaConfig.getTenant().getCoreSchema()));
        }
        return readInSchema(user, path, action, evaConfig.getTenant().getStandaloneSchema());
    }

    private <T> T readInSchema(ThreadUser user, String path, Supplier<T> action, String schema) {
        Long previousTenant = TenantContext.tenantId();
        String previousSchema = TenantContext.schemaName();
        TenantContext.bind(0L, schema);
        try {
            if (!administratorQuery.isActiveAdministrator(user.getUserId())) {
                CommonCodes.PERMISSION_DENY.newException();
            }
            List<Long> roles = user.getRolesMap() == null ? List.of() : List.copyOf(user.getRolesMap().keySet());
            if (evaConfig.getResourcePermission().isEnable()
                    && !resourceAuthorizationService.hasPermission(0L, roles, "GET", path)) {
                CommonCodes.PERMISSION_DENY.newException();
            }
            return OpenApiAuditQueryScope.read(action);
        } finally {
            if (previousTenant == null || previousSchema == null) {
                TenantContext.clear();
            } else {
                TenantContext.bind(previousTenant, previousSchema);
            }
        }
    }
}
