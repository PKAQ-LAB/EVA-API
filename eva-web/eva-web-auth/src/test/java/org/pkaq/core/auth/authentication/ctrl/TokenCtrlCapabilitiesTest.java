package org.pkaq.core.auth.authentication.ctrl;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.spi.ILoginAudit;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
import org.pkaq.core.auth.tenant.TenantLoginIdentity;
import org.pkaq.core.auth.spi.ITenantIdentityResolver;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
import org.pkaq.core.auth.spi.IAccountQuery;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
import org.pkaq.core.auth.spi.IAccountProfileQuery;
import org.pkaq.core.auth.authorization.service.AuthPermissionContextService;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.pkaq.core.exception.BizException;

import java.util.function.Supplier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 纯认证模式保留刷新令牌的权限版本撤销边界。
 *
 * @author PKAQ
 * @date 2026-10-07
 */
class TokenCtrlCapabilitiesTest {
    @Test
    void identityOnlyRefreshSignsNewPairWithoutPermissionLookup() {
        EvaConfig config = new EvaConfig();
        config.getJwt().setPersistence(false);
        JwtUtil jwt = mock(JwtUtil.class);
        TokenUtils tokens = mock(TokenUtils.class);
        IAccountQuery accounts = mock(IAccountQuery.class);
        ITenantIdentityResolver tenants = mock(ITenantIdentityResolver.class);
        ITenantAuthRouter routing = mock(ITenantAuthRouter.class);
        ILoginAudit logs = mock(ILoginAudit.class);
        CacheTokenUtil sessions = mock(CacheTokenUtil.class);
        IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
        IAccountProfileQuery profiles = mock(IAccountProfileQuery.class);
        var request = new MockHttpServletRequest();
        when(tokens.getRefreshToken(request)).thenReturn("refresh");
        when(jwt.valid("refresh")).thenReturn(true);
        when(jwt.isRefreshToken("refresh")).thenReturn(true);
        when(jwt.getUid("refresh")).thenReturn(7L);
        when(jwt.getAccount("refresh")).thenReturn("demo");
        when(jwt.getPermVer("refresh")).thenReturn(2L);
        when(jwt.getSessionId("refresh")).thenReturn("old");
        when(jwt.newSessionId()).thenReturn("new");
        when(tenants.resolveId(0L)).thenReturn(new TenantLoginIdentity(0L, 0L));
        AccountSnapshot user = new AccountSnapshot();
        user.setFrozen(FrozenEnumm.UN_FROZEN);
        user.setPermVer(2L);
        when(accounts.getAccountState(7L)).thenReturn(user);
        when(routing.execute(eq(0L), any())).thenAnswer(invocation ->
                ((Supplier<?>) invocation.getArgument(1)).get());
        when(jwt.build(config.getJwt().getAlphaTtl(), 7L, "demo", List.of(), 2L, 0L, 0L, "new"))
                .thenReturn("access-new");
        when(jwt.buildRefreshToken(config.getJwt().getBravoTtl(), 7L, "demo", List.of(), 2L, 0L, 0L, "new"))
                .thenReturn("refresh-new");
        TokenCtrl controller = new TokenCtrl(jwt, config, sessions, tokens, accounts, tenants, routing, logs,
                new AuthPermissionContextService(config, permissions, routing, profiles));

        assertTrue(controller.refreshToken(null, request, new MockHttpServletResponse()).isSuccess());

        verify(logs).rotateSession(0L, 7L, "old", "new");
        verifyNoInteractions(permissions, profiles, sessions);
    }

    @Test
    void refreshRejectsRevokedVersionWithoutLoadingResourcePermissions() {
        EvaConfig config = new EvaConfig();
        config.getJwt().setPersistence(false);
        JwtUtil jwt = mock(JwtUtil.class);
        TokenUtils tokens = mock(TokenUtils.class);
        IAccountQuery accounts = mock(IAccountQuery.class);
        ITenantIdentityResolver tenants = mock(ITenantIdentityResolver.class);
        ITenantAuthRouter routing = mock(ITenantAuthRouter.class);
        ILoginAudit logs = mock(ILoginAudit.class);
        CacheTokenUtil sessions = mock(CacheTokenUtil.class);
        var request = new MockHttpServletRequest();
        when(tokens.getRefreshToken(request)).thenReturn("refresh");
        when(jwt.valid("refresh")).thenReturn(true);
        when(jwt.isRefreshToken("refresh")).thenReturn(true);
        when(jwt.getUid("refresh")).thenReturn(7L);
        when(jwt.getPermVer("refresh")).thenReturn(1L);
        when(tenants.resolveId(0L)).thenReturn(new TenantLoginIdentity(0L, 0L));
        AccountSnapshot user = new AccountSnapshot();
        user.setFrozen(FrozenEnumm.UN_FROZEN);
        user.setPermVer(2L);
        when(accounts.getAccountState(7L)).thenReturn(user);
        when(routing.execute(eq(0L), any())).thenAnswer(invocation ->
                ((Supplier<?>) invocation.getArgument(1)).get());
        TokenCtrl controller = new TokenCtrl(jwt, config, sessions, tokens, accounts, tenants, routing, logs,
                mock(AuthPermissionContextService.class));

        assertThrows(BizException.class, () ->
                controller.refreshToken(null, request, new MockHttpServletResponse()));
        verifyNoInteractions(logs, sessions);
    }
}
