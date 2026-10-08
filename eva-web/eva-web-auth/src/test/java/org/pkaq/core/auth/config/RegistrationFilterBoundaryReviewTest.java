package org.pkaq.core.auth.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pkaq.core.auth.authentication.filter.JwtAuthFilter;
import org.pkaq.core.auth.authorization.service.AuthPermissionContextService;
import org.pkaq.core.auth.authorization.service.ResourceAuthorizationService;
import org.pkaq.core.auth.spi.IAccountProfileQuery;
import org.pkaq.core.auth.spi.IAccountRegistration;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
import org.pkaq.core.auth.spi.IResourcePermissionQuery;
import org.pkaq.core.properties.Auth;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 独立通过真实安全过滤链验证匿名通配符不能绕过注册关闭状态。
 *
 * @author Codex
 * @date 2026-10-08
 */
class RegistrationFilterBoundaryReviewTest {
    private static EvaConfig reviewConfig;
    private static IAccountRegistration registration;

    /** 注册关闭或运行模式不符合条件时，匿名全开放配置仍不得创建账号。 */
    @ParameterizedTest
    @ValueSource(strings = {"disabled", "saas", "platform", "tenant-enabled", "jwt-disabled",
            "authentication-disabled"})
    void deniesRegistrationDespiteAnonymousWildcard(String scenario) throws Exception {
        prepareConfig();
        switch (scenario) {
            case "disabled" -> reviewConfig.getAuth().getRegistration().setEnabled(false);
            case "saas", "platform" -> {
                reviewConfig.setMode(scenario);
                reviewConfig.getTenant().setEnable(true);
            }
            case "tenant-enabled" -> reviewConfig.getTenant().setEnable(true);
            case "jwt-disabled" -> {
                Auth.Jwt jwt = new Auth.Jwt();
                jwt.setEnabled(false);
                reviewConfig.getAuth().setJwt(jwt);
            }
            case "authentication-disabled" -> reviewConfig.getAuth().getAuthentication().setEnabled(false);
            default -> throw new IllegalArgumentException("未识别的测试场景");
        }
        try (var context = createContext()) {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            String response = mvc.perform(post("/api/auth/register").contextPath("/api")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertTrue(response.contains("\"success\":false"));
            verifyNoInteractions(registration);
        }
    }

    /** 仅有效开启的POST进入实际注册服务，GET不能被匿名通配符放开。 */
    @Test
    void permitsOnlyEnabledPostRegistration() throws Exception {
        prepareConfig();
        when(registration.create(anyString(), anyString(), isNull())).thenReturn(42L);
        try (var context = createContext()) {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            String response = mvc.perform(post("/api/auth/register").contextPath("/api")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("\"42\""));
            assertTrue(mvc.perform(get("/api/auth/register").contextPath("/api"))
                    .andExpect(status().isOk()).andReturn().getResponse()
                    .getContentAsString().contains("\"success\":false"));
        }
    }

    private String request() {
        return "{\"account\":\"register-review\",\"password\":\"ReviewPassword123!\"}";
    }

    private void prepareConfig() {
        reviewConfig = new EvaConfig();
        reviewConfig.setMode("standalone");
        reviewConfig.getTenant().setEnable(false);
        reviewConfig.getJwt().setPersistence(false);
        reviewConfig.getAuth().getRegistration().setEnabled(true);
        reviewConfig.getAuth().setAnonymous(new String[]{"/**"});
        Auth.OpenApi openApi = new Auth.OpenApi();
        openApi.setEnabled(false);
        reviewConfig.getAuth().setOpenApi(openApi);
        registration = mock(IAccountRegistration.class);
    }

    private AnnotationConfigWebApplicationContext createContext() {
        var context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(ReviewBeans.class);
        context.refresh();
        return context;
    }

    @Configuration
    @EnableWebMvc
    @Import(WebSecurityConfig.class)
    static class ReviewBeans extends WebSecurityCapabilitiesTest.TestBeans {
        @Bean
        @Override
        EvaConfig evaConfig() {
            return reviewConfig;
        }

        @Bean
        @Override
        IAccountRegistration accountRegistration() {
            return registration;
        }

        @Bean
        @Override
        JwtAuthFilter jwtAuthFilter() {
            return new JwtAuthFilter(jwtUtil(), reviewConfig, cacheTokenUtil(), tokenUtils(), authUserService(),
                    new ResourceAuthorizationService(reviewConfig, mock(IResourcePermissionQuery.class)),
                    tenantLoginResolver(), tenantAuthRoutingService(),
                    new AuthPermissionContextService(reviewConfig, mock(IPermissionSnapshotQuery.class),
                            tenantAuthRoutingService(), mock(IAccountProfileQuery.class)));
        }
    }
}
