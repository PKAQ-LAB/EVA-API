package org.pkaq.sys.tenant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.constant.PlatformCapabilities;
import org.pkaq.core.mybatis.tenant.TargetTenantExecutor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.organization.convert.OrganizationConvert;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.role.convert.RoleConvert;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.tenant.mapper.TenantMapper;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 平台跨租户查看身份矩阵测试。
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class PlatformTenantInspectServiceTest {
    @Mock
    private TenantMapper tenantMapper;
    @Mock
    private OrganizationMapper organizationMapper;
    @Mock
    private OrganizationConvert organizationConvert;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private RoleConvert roleConvert;
    @Mock
    private TargetTenantExecutor targetTenantExecutor;

    private EvaConfig evaConfig;
    private PlatformTenantInspectService service;

    @BeforeEach
    void setUp() {
        this.evaConfig = new EvaConfig();
        this.evaConfig.setMode(CommonConstant.MODE_PLATFORM);
        this.service = new PlatformTenantInspectService(this.evaConfig, this.tenantMapper,
                this.organizationMapper, this.organizationConvert, this.roleMapper,
                this.roleConvert, this.targetTenantExecutor);
    }

    @Test
    void permitsPlatformTenantInspector() {
        when(this.tenantMapper.selectList(any())).thenReturn(Collections.emptyList());

        ThreadUserHelper.runWithUser(platformInspector(), () ->
                assertDoesNotThrow(() -> this.service.listTenantOptions()));
    }

    @Test
    void rejectsPlatformUserWithoutCapability() {
        ThreadUser user = platformInspector().setCapabilities(Collections.emptySet());

        ThreadUserHelper.runWithUser(user, () -> assertThrows(SecurityException.class,
                () -> this.service.listTenantOptions()));
    }

    @Test
    void rejectsTenantRoleAdminEvenWithCapability() {
        ThreadUser tenantAdmin = platformInspector().setTenantId(101L);

        ThreadUserHelper.runWithUser(tenantAdmin, () -> assertThrows(SecurityException.class,
                () -> this.service.listTenantOptions()));
    }

    @Test
    void rejectsInspectorOutsidePlatformMode() {
        this.evaConfig.setMode(CommonConstant.MODE_STANDALONE);

        ThreadUserHelper.runWithUser(platformInspector(), () -> assertThrows(SecurityException.class,
                () -> this.service.listTenantOptions()));
    }

    private ThreadUser platformInspector() {
        return new ThreadUser()
                .setTenantId(0L)
                .setRolesMap(Map.of(1L,
                        new ThreadUser.GrantedRoles("平台管理员", CommonConstant.ADMIN_ROLE_NAME)))
                .setCapabilities(Set.of(PlatformCapabilities.TENANT_INSPECT));
    }
}
