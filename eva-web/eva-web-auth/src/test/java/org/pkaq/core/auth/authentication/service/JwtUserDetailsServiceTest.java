package org.pkaq.core.auth.authentication.service;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.authentication.domain.JwtUserDetail;
import org.pkaq.core.auth.authorization.service.AuthPermissionContextService;
import org.pkaq.core.auth.spi.IAccountQuery;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
import org.pkaq.core.auth.spi.IAccountProfileQuery;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
import org.pkaq.core.auth.spi.model.AccountProfileSnapshot;
import org.pkaq.core.auth.spi.model.RoleSnapshot;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.constant.PlatformCapabilities;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.properties.EvaConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 登录核心仅依赖账号和权限快照端口的行为测试。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class JwtUserDetailsServiceTest {
    @Test
    void identityLoginDoesNotQueryPermissionStorage() {
        EvaConfig config = new EvaConfig();
        IAccountQuery accounts = mock(IAccountQuery.class);
        IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
        IAccountProfileQuery profiles = mock(IAccountProfileQuery.class);
        when(accounts.getAccount("demo")).thenReturn(account());
        var service = new JwtUserDetailsService(accounts, config,
                new AuthPermissionContextService(config, permissions, mock(ITenantAuthRouter.class), profiles));

        JwtUserDetail detail = (JwtUserDetail) service.loadUserByUsername("demo");

        assertEquals("hash", detail.getPassword());
        assertEquals(2L, detail.getPermVer());
        assertTrue(detail.getAuthorities().isEmpty());
        assertEquals("demo", detail.getName());
        verifyNoInteractions(permissions, profiles);
    }

    @Test
    void dataOnlyLoginObtainsTrustedRoleSnapshot() {
        EvaConfig config = new EvaConfig();
        config.getDataPermission().setEnable(true);
        IAccountQuery accounts = mock(IAccountQuery.class);
        IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
        when(accounts.getAccount("demo")).thenReturn(account());
        RoleSnapshot role = new RoleSnapshot();
        role.setId(9L);
        role.setCode(CommonConstant.ADMIN_ROLE_NAME);
        when(permissions.findRoles(7L)).thenReturn(List.of(role));
        var service = new JwtUserDetailsService(accounts, config,
                new AuthPermissionContextService(config, permissions, mock(ITenantAuthRouter.class),
                        mock(IAccountProfileQuery.class)));

        JwtUserDetail detail = (JwtUserDetail) service.loadUserByUsername("demo");

        assertEquals(List.of(9L), detail.getRoleIds());
        assertEquals(CommonConstant.ADMIN_ROLE_NAME, detail.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void managementLoginCombinesOptionalProfileWithoutChangingAccountIdentity() {
        EvaConfig config = new EvaConfig();
        config.getResourcePermission().setEnable(true);
        IAccountQuery accounts = mock(IAccountQuery.class);
        IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
        IAccountProfileQuery profiles = mock(IAccountProfileQuery.class);
        when(accounts.getAccount("demo")).thenReturn(account());
        AccountProfileSnapshot profile = new AccountProfileSnapshot();
        profile.setAccountId(7L);
        profile.setName("管理姓名");
        profile.setDeptId(11L);
        when(profiles.findProfile(7L)).thenReturn(profile);
        var service = new JwtUserDetailsService(accounts, config,
                new AuthPermissionContextService(config, permissions, mock(ITenantAuthRouter.class), profiles));

        JwtUserDetail detail = (JwtUserDetail) service.loadUserByUsername("demo");

        assertEquals(7L, detail.getId());
        assertEquals("管理姓名", detail.getName());
        assertEquals(11L, detail.getDeptId());
        assertEquals("hash", detail.getPassword());
    }

    @Test
    void platformCapabilitiesKeepRoleLookupWithOtherPermissionsDisabled() {
        EvaConfig config = new EvaConfig();
        config.setMode("platform");
        IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
        RoleSnapshot role = new RoleSnapshot();
        role.setId(9L);
        role.setCode(CommonConstant.ADMIN_ROLE_NAME);
        when(permissions.findRoles(7L)).thenReturn(List.of(role));
        var current = new AuthPermissionContextService(config, permissions, mock(ITenantAuthRouter.class),
                mock(IAccountProfileQuery.class))
                .buildUser(7L, 0L, "demo", account());

        assertTrue(current.getCapabilities().contains(PlatformCapabilities.TENANT_INSPECT));
    }

    private AccountSnapshot account() {
        AccountSnapshot account = new AccountSnapshot();
        account.setId(7L);
        account.setAccount("demo");
        account.setPassword("hash");
        account.setFrozen(FrozenEnumm.UN_FROZEN);
        account.setPermVer(2L);
        account.setTenantId(0L);
        return account;
    }
}
