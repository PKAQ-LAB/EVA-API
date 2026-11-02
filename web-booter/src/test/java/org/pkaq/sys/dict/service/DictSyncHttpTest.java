package org.pkaq.sys.dict.service;

import org.junit.jupiter.api.Test;
import org.pkaq.core.advice.CommonResponseAdvice;
import org.pkaq.core.advice.ExceptionAdvice;
import org.pkaq.core.auth.authentication.entrypoint.UnauthorizedHandler;
import org.pkaq.core.auth.authentication.entrypoint.UrlAuthenticationFailureHandler;
import org.pkaq.core.auth.authentication.entrypoint.UrlAuthenticationSuccessHandler;
import org.pkaq.core.auth.authentication.entrypoint.UrlLogoutSuccessHandler;
import org.pkaq.core.auth.authentication.filter.JwtAuthFilter;
import org.pkaq.core.auth.authorization.entrypoint.UrlAccessDeniedHandler;
import org.pkaq.core.auth.authorization.service.AuthPermissionContextService;
import org.pkaq.core.auth.authorization.service.ResourceAuthorizationService;
import org.pkaq.core.auth.config.WebSecurityConfig;
import org.pkaq.core.auth.openapi.filter.AppKeyAuthenticationFilter;
import org.pkaq.core.auth.spi.IAccountProfileQuery;
import org.pkaq.core.auth.spi.IAccountQuery;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
import org.pkaq.core.auth.spi.IResourcePermissionQuery;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
import org.pkaq.core.auth.spi.ITenantIdentityResolver;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.i18n.I18NHelper;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.module.service.AuthorizedMenuService;
import org.pkaq.sys.user.ctrl.AuthCtrl;
import org.pkaq.sys.user.service.UserService;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 真实MVC、安全过滤链与统一Advice的条件字典响应，不启动业务服务或连接外部数据源。
 * @author PKAQ
 * @date 2026-11-01
 */
class DictSyncHttpTest {
    @Test
    void menuBootstrapReportsIndependentCurrentFlagsOnlyOnSuccess() throws Exception {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource(
                    "toggle-test", Map.of("project.version", "toggle-test")));
            context.register(Beans.class);
            context.refresh();
            authenticate(context);
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            EvaConfig config = context.getBean(EvaConfig.class);
            AuthorizedMenuService menus = context.getBean(AuthorizedMenuService.class);
            when(menus.fetchCurrentMenus()).thenReturn(java.util.List.of());
            for (boolean resourceEnabled : new boolean[]{true, false}) {
                for (boolean dataEnabled : new boolean[]{true, false}) {
                    config.getResourcePermission().setEnable(resourceEnabled);
                    config.getDataPermission().setEnable(dataEnabled);
                    mvc.perform(get("/auth/fetchMenus"))
                            .andExpect(status().isOk()).andExpect(jsonPath("success").value(true))
                            .andExpect(jsonPath("resourcePermissionEnabled").value(resourceEnabled))
                            .andExpect(jsonPath("dataPermissionEnabled").value(dataEnabled));
                }
            }
            doThrow(new IllegalStateException("synthetic menu failure")).when(menus).fetchCurrentMenus();
            mvc.perform(get("/auth/fetchMenus")).andExpect(jsonPath("success").value(false))
                    .andExpect(jsonPath("resourcePermissionEnabled").doesNotExist())
                    .andExpect(jsonPath("dataPermissionEnabled").doesNotExist());
        }
    }

    @Test
    void conditionalResponsesKeepAuthenticationAndErrorBoundaries() throws Exception {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource(
                    "sync-test", Map.of("project.version", "sync-test")));
            context.register(Beans.class);
            context.refresh();
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            DictService service = context.getBean(DictService.class);
            String etag = "W/\"dict-v1-abc\"";
            mvc.perform(get("/auth/fetchDicts").header(HttpHeaders.IF_NONE_MATCH, etag))
                    .andExpect(jsonPath("success").value(false)).andExpect(header().doesNotExist(HttpHeaders.ETAG));
            verifyNoInteractions(service);
            authenticate(context);
            when(service.snapshot()).thenAnswer(call -> {
                assertTrue(ThreadUserHelper.getCurrentUserOrNull().getUserId() == 7L);
                return new DictSnapshot(etag, Map.of("DATA_SCOPE", Map.of("0006", "指定岗位")));
            });
            mvc.perform(get("/auth/fetchDicts").header(HttpHeaders.ORIGIN, "https://example.test"))
                    .andExpect(status().isOk()).andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data.DATA_SCOPE.0006").value("指定岗位"))
                    .andExpect(header().string(HttpHeaders.ETAG, etag))
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "private, no-store"))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                            org.hamcrest.Matchers.containsString("ETag")))
                    .andExpect(result -> {
                        String vary = String.join(",", result.getResponse().getHeaders(HttpHeaders.VARY));
                        assertTrue(vary.contains("Origin") && vary.contains("Authorization")
                                && vary.contains("Cookie"));
                    });
            mvc.perform(get("/auth/fetchDicts").header(HttpHeaders.IF_NONE_MATCH, etag))
                    .andExpect(status().isNotModified()).andExpect(content().string(""))
                    .andExpect(header().string(HttpHeaders.ETAG, etag))
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "private, no-store"));
            doThrow(new DictSourceException()).when(service).snapshot();
            mvc.perform(get("/auth/fetchDicts").header(HttpHeaders.IF_NONE_MATCH, etag))
                    .andExpect(status().isConflict()).andExpect(jsonPath("success").value(false))
                    .andExpect(jsonPath("code").value("0x0000-10002"))
                    .andExpect(jsonPath("message").value("字典快照来源或结构不可用"))
                    .andExpect(header().doesNotExist(HttpHeaders.ETAG));
            doThrow(new DataAccessResourceFailureException("synthetic unavailable")).when(service).snapshot();
            mvc.perform(get("/auth/fetchDicts").header(HttpHeaders.IF_NONE_MATCH, etag))
                    .andExpect(status().isInternalServerError()).andExpect(jsonPath("success").value(false))
                    .andExpect(header().doesNotExist(HttpHeaders.ETAG));
            AccountSnapshot frozen = new AccountSnapshot();
            frozen.setFrozen(FrozenEnumm.FROZEN);
            when(context.getBean(IAccountQuery.class).getAccountState(7L)).thenReturn(frozen);
            mvc.perform(get("/auth/fetchDicts").header(HttpHeaders.IF_NONE_MATCH, etag))
                    .andExpect(jsonPath("success").value(false)).andExpect(header().doesNotExist(HttpHeaders.ETAG));
            mvc.perform(options("/auth/fetchDicts").header(HttpHeaders.ORIGIN, "https://example.test")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "If-None-Match"))
                    .andExpect(status().isOk());
            assertNull(ThreadUserHelper.getCurrentUserOrNull());
        }
    }

    private void authenticate(AnnotationConfigWebApplicationContext context) {
        JwtUtil jwt = context.getBean(JwtUtil.class);
        when(context.getBean(TokenUtils.class).getToken(any())).thenReturn("synthetic-token");
        when(jwt.valid("synthetic-token")).thenReturn(true);
        when(jwt.isAccessToken("synthetic-token")).thenReturn(true);
        when(jwt.getUid("synthetic-token")).thenReturn(7L);
        when(jwt.getAccount("synthetic-token")).thenReturn("synthetic");
        AccountSnapshot account = new AccountSnapshot();
        account.setFrozen(FrozenEnumm.UN_FROZEN);
        account.setPermVer(0L);
        when(context.getBean(IAccountQuery.class).getAccountState(7L)).thenReturn(account);
    }

    /** 只装配真实HTTP链，外部认证数据与字典服务使用本机替身。 */
    @Configuration
    @EnableWebMvc
    @Import({WebSecurityConfig.class, AuthCtrl.class, CommonResponseAdvice.class, ExceptionAdvice.class})
    static class Beans {
        @Bean EvaConfig evaConfig() {
            EvaConfig config = new EvaConfig();
            config.getJwt().setPersistence(false);
            var openApi = new org.pkaq.core.properties.Auth.OpenApi();
            openApi.setEnabled(false);
            config.getAuth().setOpenApi(openApi);
            config.getAuth().setAnonymous(new String[0]);
            config.getAuth().setPermit(new String[]{"/auth/**"});
            config.getResourcePermission().setEnable(false);
            config.getDataPermission().setEnable(false);
            return config;
        }
        @Bean I18NHelper i18nHelper() { return new I18NHelper(new StaticMessageSource()); }
        @Bean DispatcherServletPath dispatcherServletPath() { return () -> "/"; }
        @Bean AuthenticationManager authenticationManager() { return mock(AuthenticationManager.class); }
        @Bean JwtUtil jwtUtil() { return mock(JwtUtil.class); }
        @Bean CacheTokenUtil cacheTokenUtil() { return mock(CacheTokenUtil.class); }
        @Bean TokenUtils tokenUtils() { return mock(TokenUtils.class); }
        @Bean IAccountQuery accountQuery() { return mock(IAccountQuery.class); }
        @Bean ITenantIdentityResolver tenantResolver() { return mock(ITenantIdentityResolver.class); }
        @Bean ITenantAuthRouter tenantRouter() { return mock(ITenantAuthRouter.class); }
        @Bean AppKeyAuthenticationFilter appKeyFilter() { return mock(AppKeyAuthenticationFilter.class); }
        @Bean UrlAuthenticationSuccessHandler successHandler() { return mock(UrlAuthenticationSuccessHandler.class); }
        @Bean UrlAuthenticationFailureHandler failureHandler() { return mock(UrlAuthenticationFailureHandler.class); }
        @Bean UrlLogoutSuccessHandler logoutHandler() { return mock(UrlLogoutSuccessHandler.class); }
        @Bean UrlAccessDeniedHandler deniedHandler() { return new UrlAccessDeniedHandler(); }
        @Bean UnauthorizedHandler unauthorizedHandler() { return new UnauthorizedHandler(); }
        @Bean DictService dictService() { return mock(DictService.class); }
        @Bean org.pkaq.sys.dict.mapper.DictMapper dictMapper() {
            return mock(org.pkaq.sys.dict.mapper.DictMapper.class);
        }
        @Bean UserService userService() { return mock(UserService.class); }
        @Bean org.pkaq.sys.user.mapper.UserMapper userMapper() {
            return mock(org.pkaq.sys.user.mapper.UserMapper.class);
        }
        @Bean AuthorizedMenuService menuService() { return mock(AuthorizedMenuService.class); }
        @Bean JwtAuthFilter jwtAuthFilter() {
            return new JwtAuthFilter(jwtUtil(), evaConfig(), cacheTokenUtil(), tokenUtils(), accountQuery(),
                    new ResourceAuthorizationService(evaConfig(), mock(IResourcePermissionQuery.class)),
                    tenantResolver(), tenantRouter(), new AuthPermissionContextService(evaConfig(),
                    mock(IPermissionSnapshotQuery.class), tenantRouter(), mock(IAccountProfileQuery.class)));
        }
    }
}
