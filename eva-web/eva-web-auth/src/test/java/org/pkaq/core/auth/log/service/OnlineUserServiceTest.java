package org.pkaq.core.auth.log.service;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

class OnlineUserServiceTest {

    @Test
    void listsOnlyStandaloneSessionsWithoutExposingToken() {
        CacheTokenUtil tokenUtil = mock(CacheTokenUtil.class);
        doReturn(Map.of(
                "0:11", Map.of("device", "web", "token", "secret"),
                "99:12", Map.of("device", "mobile", "token", "other-secret")))
                .when(tokenUtil).getTokens(0L);
        OnlineUserService service = new OnlineUserService(tokenUtil, new EvaConfig());

        var result = service.list(null, null);

        assertEquals(1, result.size());
        assertEquals(11L, result.getFirst().getUserId());
        assertEquals("web", result.getFirst().getDevice());
        verify(tokenUtil).getTokens(0L);
    }

    @Test
    void evictsTenantAwareSessionKey() {
        CacheTokenUtil tokenUtil = mock(CacheTokenUtil.class);
        OnlineUserService service = new OnlineUserService(tokenUtil, new EvaConfig());

        service.offline(11L, null);

        verify(tokenUtil).removeToken(0L, 11L);
    }

    @Test
    void tenantUserReadsOnlyTrustedTenantPrefixAndIgnoresTargetTenant() {
        CacheTokenUtil tokenUtil = mock(CacheTokenUtil.class);
        doReturn(Map.of(
                "7:11", Map.of("device", "web", "token", "secret"),
                "70:12", Map.of("device", "other"),
                "broken", Map.of("device", "invalid")))
                .when(tokenUtil).getTokens(7L);
        EvaConfig config = new EvaConfig();
        config.setMode(CommonConstant.MODE_SAAS);
        OnlineUserService service = new OnlineUserService(tokenUtil, config);

        final List<?>[] result = new List<?>[1];
        ThreadUserHelper.runWithUser(new ThreadUser().setTenantId(7L),
                () -> result[0] = service.list(null, 99L));

        assertEquals(1, result[0].size());
        verify(tokenUtil).getTokens(7L);
    }

    @Test
    void platformAdminCanReadGlobalSessionsWhileMalformedKeysAreIgnored() {
        CacheTokenUtil tokenUtil = mock(CacheTokenUtil.class);
        doReturn(Map.of(
                "7:11", Map.of("device", "web"),
                "9:12", Map.of("device", "mobile"),
                "bad:key", Map.of("device", "invalid")))
                .when(tokenUtil).getTokens(null);
        EvaConfig config = new EvaConfig();
        config.setMode(CommonConstant.MODE_PLATFORM);
        OnlineUserService service = new OnlineUserService(tokenUtil, config);
        ThreadUser admin = new ThreadUser().setTenantId(0L).setRolesMap(Map.of(1L,
                new ThreadUser.GrantedRoles("平台管理员", CommonConstant.ADMIN_ROLE_NAME)));

        final List<?>[] result = new List<?>[1];
        ThreadUserHelper.runWithUser(admin, () -> result[0] = service.list(null, null));

        assertEquals(2, result[0].size());
        verify(tokenUtil).getTokens(null);
    }
}
