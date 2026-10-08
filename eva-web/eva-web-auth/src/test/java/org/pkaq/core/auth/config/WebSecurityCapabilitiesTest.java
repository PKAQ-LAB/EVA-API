package org.pkaq.core.auth.config;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.openapi.filter.AppKeyAuthenticationFilter;
import org.pkaq.core.auth.spi.IResourcePermissionQuery;
import org.pkaq.core.auth.authentication.entrypoint.UnauthorizedHandler;
import org.pkaq.core.auth.authorization.entrypoint.UrlAccessDeniedHandler;
import org.pkaq.core.auth.authentication.entrypoint.UrlAuthenticationFailureHandler;
import org.pkaq.core.auth.authentication.entrypoint.UrlAuthenticationSuccessHandler;
import org.pkaq.core.auth.authentication.entrypoint.UrlLogoutSuccessHandler;
import org.pkaq.core.auth.authentication.filter.JwtAuthFilter;
import org.pkaq.core.auth.authorization.service.AuthPermissionContextService;
import org.pkaq.core.auth.authorization.service.ResourceAuthorizationService;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
import org.pkaq.core.auth.spi.ITenantIdentityResolver;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
import org.pkaq.core.auth.spi.IAccountQuery;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.auth.spi.IAccountProfileQuery;
import org.pkaq.core.properties.Auth;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * 使用真实 Spring Security 过滤链验证能力开关的 HTTP 边界。
 *
 * @author PKAQ
 * @date 2026-10-07
 */
class WebSecurityCapabilitiesTest {
    private static EvaConfig testConfig;

    @Test
    void disabledAuthenticationOnlyAllowsExplicitPublicEndpoints() throws Exception {
        try (var context = createContext(false)) {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            mvc.perform(get("/public/ping")).andExpect(status().isOk()).andExpect(content().string("ok"));
            assertTrue(mvc.perform(get("/sys/user/list")).andExpect(status().isOk()).andReturn().getResponse()
                    .getContentAsString().contains("\"success\":false"));
            assertTrue(mvc.perform(get("/auth/getAlpha")).andExpect(status().isOk()).andReturn().getResponse()
                    .getContentAsString().contains("\"success\":false"));
            assertNull(ThreadUserHelper.getCurrentUserOrNull());
        }
    }

    @Test
    void defaultAuthenticationRejectsAnonymousAndAcceptsValidJwt() throws Exception {
        try (var context = createContext(true)) {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            assertTrue(mvc.perform(get("/sys/user/list")).andExpect(status().isOk()).andReturn().getResponse()
                    .getContentAsString().contains("\"success\":false"));
            JwtUtil jwt = context.getBean(JwtUtil.class);
            when(context.getBean(TokenUtils.class).getToken(any())).thenReturn("valid-token");
            when(jwt.valid("valid-token")).thenReturn(true);
            when(jwt.isAccessToken("valid-token")).thenReturn(true);
            when(jwt.getUid("valid-token")).thenReturn(7L);
            when(jwt.getAccount("valid-token")).thenReturn("demo");
            AccountSnapshot user = new AccountSnapshot();
            user.setFrozen(FrozenEnumm.UN_FROZEN);
            user.setPermVer(0L);
            when(context.getBean(IAccountQuery.class).getAccountState(7L)).thenReturn(user);
            mvc.perform(get("/sys/user/list")).andExpect(status().isOk()).andExpect(content().string("ok"));
            assertNull(ThreadUserHelper.getCurrentUserOrNull());
        }
    }

    private AnnotationConfigWebApplicationContext createContext(boolean enabled) {
        testConfig = new EvaConfig();
        testConfig.getAuth().getAuthentication().setEnabled(enabled);
        testConfig.getJwt().setPersistence(false);
        Auth.OpenApi openApi = new Auth.OpenApi();
        openApi.setEnabled(false);
        testConfig.getAuth().setOpenApi(openApi);
        testConfig.getAuth().setAnonymous(new String[]{"/public/**", "/auth/**"});
        var context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestBeans.class);
        context.refresh();
        return context;
    }

    @Configuration
    @EnableWebMvc
    @Import(WebSecurityConfig.class)
    static class TestBeans {
        @Bean EvaConfig evaConfig() { return testConfig; }
        @Bean DispatcherServletPath dispatcherServletPath() { return () -> "/"; }
        @Bean AuthenticationManager authenticationManager() { return mock(AuthenticationManager.class); }
        @Bean JwtUtil jwtUtil() { return mock(JwtUtil.class); }
        @Bean CacheTokenUtil cacheTokenUtil() { return mock(CacheTokenUtil.class); }
        @Bean TokenUtils tokenUtils() { return mock(TokenUtils.class); }
        @Bean IAccountQuery authUserService() { return mock(IAccountQuery.class); }
        @Bean ITenantIdentityResolver tenantLoginResolver() { return mock(ITenantIdentityResolver.class); }
        @Bean ITenantAuthRouter tenantAuthRoutingService() { return mock(ITenantAuthRouter.class); }
        @Bean AppKeyAuthenticationFilter appKeyFilter() { return mock(AppKeyAuthenticationFilter.class); }
        @Bean UrlAuthenticationSuccessHandler successHandler() { return mock(UrlAuthenticationSuccessHandler.class); }
        @Bean UrlAuthenticationFailureHandler failureHandler() { return mock(UrlAuthenticationFailureHandler.class); }
        @Bean UrlLogoutSuccessHandler logoutHandler() { return mock(UrlLogoutSuccessHandler.class); }
        @Bean UrlAccessDeniedHandler deniedHandler() { return new UrlAccessDeniedHandler(); }
        @Bean UnauthorizedHandler unauthorizedHandler() { return new UnauthorizedHandler(); }
        @Bean TestEndpoints endpoints() { return new TestEndpoints(); }
        @Bean JwtAuthFilter jwtAuthFilter() {
            return new JwtAuthFilter(jwtUtil(), testConfig, cacheTokenUtil(), tokenUtils(), authUserService(),
                    new ResourceAuthorizationService(testConfig, mock(IResourcePermissionQuery.class)),
                    tenantLoginResolver(), tenantAuthRoutingService(),
                    new AuthPermissionContextService(testConfig, mock(IPermissionSnapshotQuery.class),
                            tenantAuthRoutingService(), mock(IAccountProfileQuery.class)));
        }
    }

    @RestController
    static class TestEndpoints {
        @GetMapping({"/public/ping", "/sys/user/list", "/auth/getAlpha"})
        String ping() { return "ok"; }
    }
}
