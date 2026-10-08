package org.pkaq.core.auth.authentication.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.auth.spi.IAccountQuery;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
import org.pkaq.core.auth.authorization.service.AuthPermissionContextService;
import org.pkaq.core.auth.authorization.service.ResourceAuthorizationService;
import org.pkaq.core.auth.spi.IResourcePermissionQuery;
import org.pkaq.core.auth.spi.model.RoleSnapshot;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
import org.pkaq.core.auth.spi.ITenantIdentityResolver;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.properties.Auth;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.Jwt;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JwtAuthFilter 单元测试
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private CacheTokenUtil cacheTokenUtil;
    @Mock
    private TokenUtils tokenUtil;
    @Mock
    private IAccountQuery authUserService;
    @Mock
    private IPermissionSnapshotQuery permissionQuery;
    @Mock
    private IResourcePermissionQuery roleResourceCacheService;
    @Mock
    private ITenantIdentityResolver tenantLoginResolver;
    @Mock
    private ITenantAuthRouter tenantAuthRoutingService;
    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectRequestWhenPermVerChanged() throws Exception {
        EvaConfig evaConfig = buildEvaConfig();
        JwtAuthFilter filter = new JwtAuthFilter(
                jwtUtil,
                evaConfig,
                cacheTokenUtil,
                tokenUtil,
                authUserService,
                new ResourceAuthorizationService(evaConfig, roleResourceCacheService),
                tenantLoginResolver,
                tenantAuthRoutingService,
                new AuthPermissionContextService(evaConfig, permissionQuery, tenantAuthRoutingService)
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/list");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenUtil.getToken(request)).thenReturn("mock-token");
        when(jwtUtil.getUid("mock-token")).thenReturn(1001L);
        when(jwtUtil.valid("mock-token")).thenReturn(true);
        when(jwtUtil.isAccessToken("mock-token")).thenReturn(true);
        when(jwtUtil.isTokenExpiring("mock-token")).thenReturn(false);
        when(jwtUtil.getAccount("mock-token")).thenReturn("admin");
        when(jwtUtil.getPermVer("mock-token")).thenReturn(1L);
        AccountSnapshot authState = new AccountSnapshot();
        authState.setFrozen(FrozenEnumm.UN_FROZEN);
        authState.setPermVer(2L);
        when(authUserService.getAccountState(1001L)).thenReturn(authState);

        filter.doFilter(request, response, filterChain);

        assertTrue(response.getContentAsString().contains("\"code\":\"0x000-00023\""));
        assertFalse(response.getContentAsString().contains("\"success\":true"));
        verify(filterChain, never()).doFilter(request, response);
        verify(roleResourceCacheService, never())
                .hasPermission(0L, List.of(1L, 2L), "GET", "/api/user/list");
    }

    @Test
    void shouldBuildThreadUserFromTrustedServerRoles() throws Exception {
        EvaConfig evaConfig = buildEvaConfig();
        JwtAuthFilter filter = new JwtAuthFilter(
                jwtUtil, evaConfig, cacheTokenUtil, tokenUtil, authUserService,
                new ResourceAuthorizationService(evaConfig, roleResourceCacheService),
                tenantLoginResolver, tenantAuthRoutingService,
                new AuthPermissionContextService(evaConfig, permissionQuery, tenantAuthRoutingService));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/list");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenUtil.getToken(request)).thenReturn("mock-token");
        when(jwtUtil.getUid("mock-token")).thenReturn(1001L);
        when(jwtUtil.valid("mock-token")).thenReturn(true);
        when(jwtUtil.isAccessToken("mock-token")).thenReturn(true);
        when(jwtUtil.isTokenExpiring("mock-token")).thenReturn(false);
        when(jwtUtil.getAccount("mock-token")).thenReturn("admin-account");
        when(jwtUtil.getPermVer("mock-token")).thenReturn(3L);
        when(jwtUtil.getTenantId("mock-token")).thenReturn(8L);
        RoleSnapshot adminRole = new RoleSnapshot();
        adminRole.setId(10L);
        adminRole.setName("平台管理员");
        adminRole.setCode(CommonConstant.ADMIN_ROLE_NAME);
        AccountSnapshot authState = new AccountSnapshot();
        authState.setFrozen(FrozenEnumm.UN_FROZEN);
        authState.setPermVer(3L);
        authState.setName("管理员");
        evaConfig.getDataPermission().setEnable(true);
        when(permissionQuery.findRoles(1001L)).thenReturn(List.of(adminRole));
        when(authUserService.getAccountState(1001L)).thenReturn(authState);
        doAnswer(invocation -> {
            assertEquals("admin-account", ThreadUserHelper.getAccount());
            assertEquals("管理员", ThreadUserHelper.getUserName());
            assertEquals(8L, ThreadUserHelper.getTenantId());
            assertTrue(ThreadUserHelper.isAdmin());
            assertTrue(ThreadUserHelper.getUsetGrantedRoles().containsKey(10L));
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getRoles("mock-token");
    }

    /**
     * 构造测试所需的认证配置。
     */
    private EvaConfig buildEvaConfig() {
        EvaConfig evaConfig = new EvaConfig();

        Jwt jwt = new Jwt();
        jwt.setPersistence(false);
        evaConfig.setJwt(jwt);

        Auth auth = new Auth();
        Auth.Jwt authJwt = new Auth.Jwt();
        authJwt.setEnabled(true);
        authJwt.setPaths(new String[]{"/api/**"});
        auth.setJwt(authJwt);
        auth.setPermit(new String[0]);
        evaConfig.setAuth(auth);

        return evaConfig;
    }
}
