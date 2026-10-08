package org.pkaq.core.auth.authentication.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.pkaq.core.auth.openapi.filter.AppKeyAuthenticationFilter;
import org.pkaq.core.auth.openapi.security.SignatureValidator;
import org.pkaq.core.auth.spi.IAppCredentialQuery;
import org.pkaq.core.auth.spi.IOpenApiAudit;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.web.core.utils.TokenUtils;
import org.pkaq.core.exception.BizException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 精确公开注册路径不应因残留OpenAPI凭据被认证过滤器阻断。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class RegistrationFilterBoundaryTest {
    @BeforeEach
    void clearPreviousAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtRegistrationWithEmptyServletPathStripsActualContextAndBypassesTokenRead() throws Exception {
        EvaConfig config = enabledConfig();
        TokenUtils tokens = mock(TokenUtils.class);
        JwtUtil jwt = mock(JwtUtil.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, config, mock(CacheTokenUtil.class), tokens,
                null, null, null, null, null);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");
        request.setContextPath("/api");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verifyNoInteractions(tokens, jwt);
    }

    @Test
    void disabledRegistrationDoesNotBypassJwtAuthentication() throws Exception {
        EvaConfig config = enabledConfig();
        config.getAuth().getRegistration().setEnabled(false);
        TokenUtils tokens = mock(TokenUtils.class);
        JwtAuthFilter filter = new JwtAuthFilter(mock(JwtUtil.class), config, mock(CacheTokenUtil.class), tokens,
                null, null, null, null, null);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");
        request.setContextPath("/api");
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        verify(tokens).getToken(request);
    }

    @Test
    void contextFallbackNeverStripsLookalikePrefixAndPreservesServletPath() {
        EvaConfig config = enabledConfig();
        Object[] filters = {new JwtAuthFilter(null, config, null, null, null, null, null, null, null),
                new AppKeyAuthenticationFilter(null, null, null, config)};
        for (Object filter : filters) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/apix/auth/register");
            request.setContextPath("/api");
            assertEquals("/apix/auth/register",
                    ReflectionTestUtils.invokeMethod(filter, "resolveRequestPath", request));
            request.setRequestURI("/api/auth/register");
            assertEquals("/auth/register",
                    ReflectionTestUtils.invokeMethod(filter, "resolveRequestPath", request));
            request.setRequestURI("/api");
            assertEquals("", ReflectionTestUtils.invokeMethod(filter, "resolveRequestPath", request));
            request.setServletPath("/explicit/path");
            assertEquals("/explicit/path",
                    ReflectionTestUtils.invokeMethod(filter, "resolveRequestPath", request));
        }
    }

    @Test
    void disabledRegistrationStillChecksMalformedOpenApiCredentials() {
        EvaConfig config = enabledConfig();
        config.getAuth().getRegistration().setEnabled(false);
        AppKeyAuthenticationFilter filter = new AppKeyAuthenticationFilter(mock(IAppCredentialQuery.class),
                mock(SignatureValidator.class), mock(IOpenApiAudit.class), config);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");
        request.setContextPath("/api");
        var headers = config.getAuth().getOpenApi().getHeaders();
        request.addHeader(headers.getAppKey(), "old-app-key");
        request.addHeader(headers.getTimestamp(), "invalid-timestamp");
        request.addHeader(headers.getSignature(), "bad-signature");
        assertThrows(BizException.class,
                () -> filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class)));
    }

    @Test
    void publicRegistrationBypassesEvenMalformedOpenApiHeaders() throws Exception {
        EvaConfig config = new EvaConfig();
        config.setMode("standalone");
        config.getTenant().setEnable(false);
        config.getAuth().getAuthentication().setEnabled(true);
        config.getAuth().getJwt().setEnabled(true);
        config.getAuth().getRegistration().setEnabled(true);
        IAppCredentialQuery credentials = mock(IAppCredentialQuery.class);
        SignatureValidator validator = mock(SignatureValidator.class);
        IOpenApiAudit audit = mock(IOpenApiAudit.class);
        AppKeyAuthenticationFilter filter = new AppKeyAuthenticationFilter(credentials, validator, audit, config);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");
        request.setContextPath("/api");
        var headers = config.getAuth().getOpenApi().getHeaders();
        request.addHeader(headers.getAppKey(), "old-app-key");
        request.addHeader(headers.getTimestamp(), "invalid-timestamp");
        request.addHeader(headers.getSignature(), "bad-signature");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(credentials, validator, audit);
    }

    private EvaConfig enabledConfig() {
        EvaConfig config = new EvaConfig();
        config.setMode("standalone");
        config.getTenant().setEnable(false);
        config.getAuth().getAuthentication().setEnabled(true);
        config.getAuth().getJwt().setEnabled(true);
        config.getAuth().getRegistration().setEnabled(true);
        return config;
    }
}
