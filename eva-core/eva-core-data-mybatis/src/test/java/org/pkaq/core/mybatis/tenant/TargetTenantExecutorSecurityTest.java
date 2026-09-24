package org.pkaq.core.mybatis.tenant;

import org.junit.jupiter.api.Test;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 目标租户执行身份边界测试。
 *
 * @author PKAQ
 */
class TargetTenantExecutorSecurityTest {
    @Test
    void tenantRoleAdminCannotInspectAnotherTenant() {
        TargetTenantExecutor executor = new TargetTenantExecutor(platformSchemaConfig(), null);
        ThreadUser tenantAdmin = new ThreadUser()
                .setTenantId(101L)
                .setRolesMap(Map.of(1L,
                        new ThreadUser.GrantedRoles("租户管理员", CommonConstant.ADMIN_ROLE_NAME)));

        ThreadUserHelper.runWithUser(tenantAdmin, () -> assertThrows(SecurityException.class,
                () -> executor.execute(102L, () -> "denied")));
    }

    @Test
    void rejectsInvalidTargetTenantBeforeRouting() {
        TargetTenantExecutor executor = new TargetTenantExecutor(platformSchemaConfig(), null);
        ThreadUser platformAdmin = new ThreadUser()
                .setTenantId(0L)
                .setRolesMap(Map.of(1L,
                        new ThreadUser.GrantedRoles("平台管理员", CommonConstant.ADMIN_ROLE_NAME)));

        ThreadUserHelper.runWithUser(platformAdmin, () -> assertThrows(SecurityException.class,
                () -> executor.execute(0L, () -> "denied")));
    }

    private EvaConfig platformSchemaConfig() {
        EvaConfig config = new EvaConfig();
        config.setMode(CommonConstant.MODE_PLATFORM);
        TenantProperties tenant = new TenantProperties();
        tenant.setEnable(true);
        tenant.setMode(TenantProperties.MODE_SCHEMA);
        config.setTenant(tenant);
        return config;
    }
}
