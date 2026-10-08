package org.pkaq.core.auth.authorization.service;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.spi.model.RoleSnapshot;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
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
        assertTrue(new AuthPermissionContextService(config, permissions, mock(ITenantAuthRouter.class))
                .buildUser(1L, 0L, "demo", new AccountSnapshot())
                .getDataScopes().isEmpty());
        verifyNoInteractions(permissions);
    }

    @Test
    void dataScopeWorksWithoutResourcePermissionAndDropsInvalidOrganizations() {
        EvaConfig config = new EvaConfig();
        config.getDataPermission().setEnable(true);
        IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
        when(permissions.findRoles(1L)).thenReturn(List.of(roleWithScope("2,2,-1,abc,3")));
        AccountSnapshot user = new AccountSnapshot();
        user.setDeptId(7L);
        var current = new AuthPermissionContextService(config, permissions, mock(ITenantAuthRouter.class))
                .buildUser(1L, 0L, "demo", user);
        assertEquals(List.of(2L, 3L), current.getDataScopes().getFirst().getOrgIds());
        assertEquals(7L, current.getDeptId());
    }

    private RoleSnapshot roleWithScope(String organizations) {
        RoleSnapshot role = new RoleSnapshot();
        role.setId(1L);
        role.setDataScope("CUSTOM");
        role.setDataOrgIds(organizations);
        return role;
    }
}
