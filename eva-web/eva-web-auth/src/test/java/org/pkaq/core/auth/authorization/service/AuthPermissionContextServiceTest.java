package org.pkaq.core.auth.authorization.service;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.spi.model.RoleSnapshot;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
import org.pkaq.core.auth.spi.model.AccountProfileSnapshot;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
import org.pkaq.core.auth.spi.IAccountProfileQuery;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
import org.pkaq.core.properties.EvaConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 数据权限快照的独立开关回归测试。
 *
 * @author PKAQ
 * @date 2026-10-07
 */
class AuthPermissionContextServiceTest {
    @Test
    void dataScopeIsNotParsedWhenDisabled() {
        EvaConfig config = new EvaConfig();
        IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
        IAccountProfileQuery profiles = mock(IAccountProfileQuery.class);
        assertTrue(new AuthPermissionContextService(config, permissions, mock(ITenantAuthRouter.class), profiles)
                .buildUser(1L, 0L, "demo", new AccountSnapshot())
                .getDataScopes().isEmpty());
        verifyNoInteractions(permissions, profiles);
    }

    @Test
    void dataScopeWorksWithoutResourcePermissionAndDropsInvalidOrganizations() {
        EvaConfig config = new EvaConfig();
        config.getDataPermission().setEnable(true);
        IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
        when(permissions.findRoles(1L)).thenReturn(List.of(roleWithScope("2,2,-1,abc,3")));
        AccountSnapshot user = new AccountSnapshot();
        IAccountProfileQuery profiles = mock(IAccountProfileQuery.class);
        AccountProfileSnapshot profile = new AccountProfileSnapshot();
        profile.setDeptId(7L);
        when(profiles.findProfile(1L)).thenReturn(profile);
        var current = new AuthPermissionContextService(config, permissions, mock(ITenantAuthRouter.class), profiles)
                .buildUser(1L, 0L, "demo", user);
        assertEquals(List.of(2L, 3L), current.getDataScopes().getFirst().getOrgIds());
        assertEquals(7L, current.getDeptId());
    }

    @Test
    void missingManagementProfileDoesNotBlockPermissionContext() {
        EvaConfig config = new EvaConfig();
        config.getResourcePermission().setEnable(true);
        IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
        IAccountProfileQuery profiles = mock(IAccountProfileQuery.class);
        AccountSnapshot user = new AccountSnapshot();
        user.setNickName("昵称");
        var current = new AuthPermissionContextService(config, permissions, mock(ITenantAuthRouter.class), profiles)
                .buildUser(1L, 0L, "demo", user);
        assertEquals("昵称", current.getName());
        assertEquals(0L, current.getDeptId());
        assertTrue(current.getRolesMap().isEmpty());
    }

    private RoleSnapshot roleWithScope(String organizations) {
        RoleSnapshot role = new RoleSnapshot();
        role.setId(1L);
        role.setDataScope("CUSTOM");
        role.setDataOrgIds(organizations);
        return role;
    }

    @Test
    void rebuildsPostEligibilityEveryRequestAndFailsClosedWithoutChangingOtherRoles() {
        EvaConfig config = new EvaConfig();
        config.getDataPermission().setEnable(true);
        IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
        RoleSnapshot post = roleWithScope(null);
        post.setDataScope("0006");
        post.setDeleted(0L);
        RoleSnapshot self = roleWithScope(null);
        self.setId(2L);
        self.setDataScope("0007");
        when(permissions.findRoles(7L)).thenReturn(List.of(post, self));
        when(permissions.findEligiblePostScopes(7L)).thenReturn(java.util.Map.of(1L, List.of(11L, 12L)))
                .thenReturn(java.util.Map.of()).thenThrow(new IllegalStateException("合成来源失败"));
        var service = new AuthPermissionContextService(config, permissions,
                mock(ITenantAuthRouter.class), mock(IAccountProfileQuery.class));
        var first = service.buildUser(7L, 0L, "合成账号", new AccountSnapshot());
        assertTrue(first.getDataScopes().getFirst().isPostEligible());
        assertEquals(List.of(11L, 12L), first.getDataScopes().getFirst().getPostIds());
        assertTrue(!service.buildUser(7L, 0L, "合成账号", new AccountSnapshot())
                .getDataScopes().getFirst().isPostEligible());
        var failed = service.buildUser(7L, 0L, "合成账号", new AccountSnapshot());
        assertTrue(!failed.getDataScopes().getFirst().isPostEligible());
        assertEquals("0007", failed.getDataScopes().get(1).getCode());
        org.mockito.Mockito.verify(permissions, org.mockito.Mockito.times(3)).findEligiblePostScopes(7L);
    }
}
