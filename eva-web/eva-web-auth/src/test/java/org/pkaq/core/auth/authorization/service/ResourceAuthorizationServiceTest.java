package org.pkaq.core.auth.authorization.service;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.spi.IResourcePermissionQuery;
import org.pkaq.core.properties.Auth;
import org.pkaq.core.properties.EvaConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 资源权限开关与可信角色回归测试。
 *
 * @author PKAQ
 * @date 2026-10-07
 */
class ResourceAuthorizationServiceTest {
    @Test
    void disabledPermissionDoesNotAccessResourceCache() {
        EvaConfig config = new EvaConfig();
        IResourcePermissionQuery cache = mock(IResourcePermissionQuery.class);
        assertTrue(new ResourceAuthorizationService(config, cache)
                .hasPermission(0L, List.of(), "GET", "/sys/user/list"));
        verifyNoInteractions(cache);
    }

    @Test
    void enabledPermissionRequiresRoleAndMatchesPermitPaths() {
        EvaConfig config = new EvaConfig();
        config.getResourcePermission().setEnable(true);
        Auth auth = new Auth();
        auth.setPermit(new String[]{"/auth/**"});
        config.setAuth(auth);
        IResourcePermissionQuery cache = mock(IResourcePermissionQuery.class);
        ResourceAuthorizationService service = new ResourceAuthorizationService(config, cache);
        assertTrue(service.hasPermission(0L, List.of(), "GET", "/auth/info"));
        assertFalse(service.hasPermission(0L, List.of(), "GET", "/sys/user/list"));
        verifyNoInteractions(cache);
        when(cache.hasPermission(0L, List.of(1L), "GET", "/sys/user/list")).thenReturn(true);
        assertTrue(service.hasPermission(0L, List.of(1L), "GET", "/sys/user/list"));
    }
}
