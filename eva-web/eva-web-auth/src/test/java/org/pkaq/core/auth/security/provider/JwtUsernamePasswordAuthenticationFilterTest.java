package org.pkaq.core.auth.security.provider;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.tenant.TenantLoginIdentity;
import org.pkaq.core.auth.tenant.TenantLoginResolver;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 租户登录请求契约测试。
 *
 * @author PKAQ
 */
class JwtUsernamePasswordAuthenticationFilterTest {

    @Test
    void shouldKeepStandaloneAccountPasswordRequestCompatible() {
        EvaConfig config = config(false);
        TenantLoginResolver resolver = mock(TenantLoginResolver.class);
        AuthenticationManager manager = authentication -> authentication;
        JwtUsernamePasswordAuthenticationFilter filter = filter(config, resolver, manager);
        MockHttpServletRequest request = request("{\"account\":\"admin\",\"password\":\"secret\"}");

        var result = filter.attemptAuthentication(request, new MockHttpServletResponse());

        assertInstanceOf(UsernamePasswordAuthenticationToken.class, result);
        assertEquals("admin", result.getPrincipal());
    }

    @Test
    void shouldRequireTrustedTenantCodeInSchemaMode() {
        EvaConfig config = config(true);
        TenantLoginResolver resolver = mock(TenantLoginResolver.class);
        when(resolver.resolveCode(null)).thenThrow(new IllegalArgumentException("tenantCode required"));
        JwtUsernamePasswordAuthenticationFilter filter = filter(config, resolver, authentication -> authentication);

        assertThrows(BadCredentialsException.class,
                () -> filter.attemptAuthentication(request("{\"account\":\"admin\",\"password\":\"secret\"}"),
                        new MockHttpServletResponse()));
    }

    @Test
    void shouldRejectClientSchemaName() {
        JwtUsernamePasswordAuthenticationFilter filter = filter(config(true), mock(TenantLoginResolver.class),
                authentication -> authentication);

        assertThrows(BadCredentialsException.class,
                () -> filter.attemptAuthentication(request("""
                        {"account":"admin","password":"secret","tenantCode":"demo","schemaName":"tenant_demo"}
                        """), new MockHttpServletResponse()));
    }

    @Test
    void shouldCarryOnlyResolvedTenantId() {
        TenantLoginResolver resolver = mock(TenantLoginResolver.class);
        when(resolver.resolveCode("demo")).thenReturn(new TenantLoginIdentity(42L, 7L));
        JwtUsernamePasswordAuthenticationFilter filter = filter(config(true), resolver,
                authentication -> authentication);

        var result = filter.attemptAuthentication(request("""
                {"account":"admin","password":"secret","tenantCode":"demo"}
                """), new MockHttpServletResponse());

        TenantLoginAuthenticationToken token = assertInstanceOf(TenantLoginAuthenticationToken.class, result);
        assertEquals(42L, token.getTenantId());
    }

    private JwtUsernamePasswordAuthenticationFilter filter(EvaConfig config, TenantLoginResolver resolver,
                                                            AuthenticationManager manager) {
        return new JwtUsernamePasswordAuthenticationFilter("/auth/login", manager,
                mock(AuthenticationSuccessHandler.class), mock(AuthenticationFailureHandler.class), config, resolver);
    }

    private MockHttpServletRequest request(String json) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.setContentType("application/json");
        request.setContent(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return request;
    }

    private EvaConfig config(boolean schemaMode) {
        EvaConfig config = new EvaConfig();
        TenantProperties tenant = new TenantProperties();
        tenant.setEnable(schemaMode);
        tenant.setMode(schemaMode ? TenantProperties.MODE_SCHEMA : TenantProperties.MODE_STANDALONE);
        config.setTenant(tenant);
        return config;
    }
}
