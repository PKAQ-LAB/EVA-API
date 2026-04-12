package org.pkaq.core.auth.security.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.auth.user.service.AuthUserService;
import org.pkaq.core.auth.rbac.service.RoleResourceCacheService;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.properties.Auth;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.Jwt;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private AuthUserService authUserService;
    @Mock
    private RoleResourceCacheService roleResourceCacheService;
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
                roleResourceCacheService
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/list");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenUtil.getToken(request)).thenReturn("mock-token");
        when(jwtUtil.getUid("mock-token")).thenReturn(1001L);
        when(jwtUtil.valid("mock-token")).thenReturn(true);
        when(jwtUtil.isTokenExpiring("mock-token")).thenReturn(false);
        when(jwtUtil.getAccount("mock-token")).thenReturn("admin");
        when(jwtUtil.getRoles("mock-token")).thenReturn(List.of(1L, 2L));
        when(jwtUtil.getPermVer("mock-token")).thenReturn(1L);
        when(authUserService.getPermVer(1001L)).thenReturn(2L);

        filter.doFilter(request, response, filterChain);

        assertTrue(response.getContentAsString().contains("\"code\":\"0x000-00023\""));
        assertFalse(response.getContentAsString().contains("\"success\":true"));
        verify(filterChain, never()).doFilter(request, response);
        verify(roleResourceCacheService, never())
                .hasPermission(List.of(1L, 2L), "GET", "/api/user/list");
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
