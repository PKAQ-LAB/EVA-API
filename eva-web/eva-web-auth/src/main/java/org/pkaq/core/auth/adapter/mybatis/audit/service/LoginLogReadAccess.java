package org.pkaq.core.auth.adapter.mybatis.audit.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.authorization.service.ResourceAuthorizationService;
import org.pkaq.core.auth.spi.IOpenApiAuditAdministratorQuery;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mybatis.log.LoginLogQueryScope;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

/**
 * 历史登录日志常驻资格；角色快照、ALL 与权限开关不能替代管理员关系。
 * @author PKAQ
 * @date 2026-10-30
 */
@Service
@RequiredArgsConstructor
public class LoginLogReadAccess {
    private final EvaConfig evaConfig;
    private final LoginLogStorage storage;
    private final IOpenApiAuditAdministratorQuery administratorQuery;
    private final ITenantAuthRouter tenantAuthRouter;
    private final JdbcTemplate jdbcTemplate;
    private final ResourceAuthorizationService resourceAuthorizationService;

    /** 校验管理员及目标范围后，执行同步固定租户读取。 */
    public <T> T read(String path, Long targetTenantId, Supplier<T> action) {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        boolean standalone = evaConfig.isStandaloneMode() && !evaConfig.getTenant().isSchemaMode();
        boolean schema = evaConfig.getTenant().isSchemaMode()
                && (evaConfig.isPlatformMode() || evaConfig.isSaasMode());
        if (user == null || user.getUserId() <= 0L || user.getTenantId() < 0L
                || !standalone && !schema || standalone && user.getTenantId() != 0L
                || schema && user.getTenantId() == 0L && !evaConfig.isPlatformMode()
                || targetTenantId != null && targetTenantId < 0L) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        Long incomingTenant = TenantContext.tenantId();
        String incomingSchema = TenantContext.schemaName();
        if (incomingTenant != null && incomingTenant != user.getTenantId()) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        boolean global = schema && user.getTenantId() == 0L;
        Long filter = global ? targetTenantId : Long.valueOf(user.getTenantId());
        if (!global && targetTenantId != null && !targetTenantId.equals(filter)) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        return storage.read(() -> {
            if (user.getTenantId() == 0L) {
                String expected = standalone ? evaConfig.getTenant().getStandaloneSchema()
                        : evaConfig.getTenant().getCoreSchema();
                if (incomingSchema != null && !expected.equals(incomingSchema)) {
                    CommonCodes.PERMISSION_DENY.newException();
                }
                storage.requireRelations(expected, "sys_account", "sys_roleuser_ref", "sys_role");
                if (!administratorQuery.isActiveAdministrator(user.getUserId())) {
                    CommonCodes.PERMISSION_DENY.newException();
                }
                requireResource(user, path);
            } else {
                storage.requireRelations(evaConfig.getTenant().getCoreSchema(), "sys_tenant");
                tenantAuthRouter.execute(user.getTenantId(), () -> {
                    String privateSchema = TenantContext.schemaName();
                    if (!Long.valueOf(user.getTenantId()).equals(TenantContext.tenantId())
                            || incomingSchema != null && !incomingSchema.equals(privateSchema)) {
                        CommonCodes.PERMISSION_DENY.newException();
                    }
                    storage.requireRelations(privateSchema, "sys_account", "sys_roleuser_ref", "sys_role");
                    Boolean administrator = jdbcTemplate.queryForObject("""
                            SELECT EXISTS (
                                SELECT 1 FROM SYS_ACCOUNT account
                                JOIN SYS_ROLEUSER_REF membership ON membership.USER_ID = account.ID
                                JOIN SYS_ROLE role ON role.ID = membership.ROLE_ID
                                WHERE account.ID = ? AND account.DELETED = 0 AND account.FROZEN IN (0, 9999)
                                  AND role.CODE = 'TENANT_ADMIN' AND role.DELETED = 0 AND role.FROZEN = 9999
                            )
                            """, Boolean.class, user.getUserId());
                    if (!Boolean.TRUE.equals(administrator)) {
                        CommonCodes.PERMISSION_DENY.newException();
                    }
                    requireResource(user, path);
                    return null;
                });
            }
            return LoginLogQueryScope.read(filter, action);
        });
    }

    private void requireResource(ThreadUser user, String path) {
        List<Long> roles = user.getRolesMap() == null ? List.of() : List.copyOf(user.getRolesMap().keySet());
        if (evaConfig.getResourcePermission().isEnable()
                && !resourceAuthorizationService.hasPermission(user.getTenantId(), roles, "GET", path)) {
            CommonCodes.PERMISSION_DENY.newException();
        }
    }
}
