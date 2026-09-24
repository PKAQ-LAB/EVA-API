package org.pkaq.sys.tenant;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.pkaq.core.mybatis.tenant.TargetTenantExecutor;
import org.pkaq.core.i18n.I18NHelper;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.sys.organization.convert.OrganizationConvert;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.role.convert.RoleConvert;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.tenant.ctrl.PlatformTenantInspectCtrl;
import org.pkaq.sys.tenant.mapper.TenantMapper;
import org.pkaq.sys.tenant.service.PlatformTenantInspectService;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * 平台跨租户只读接口条件装配测试。
 *
 * @author PKAQ
 */
class PlatformTenantInspectConditionalContextTest {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void standaloneDoesNotRegisterControllerOrEndpoint() {
        this.contextRunner.withPropertyValues("eva.mode=standalone").run(context -> {
            assertEquals(0, context.getBeansOfType(PlatformTenantInspectCtrl.class).size());
            assertEquals(0, context.getBeansOfType(PlatformTenantInspectService.class).size());
            assertEquals(Set.of(), platformMappings(context.getBean(RequestMappingHandlerMapping.class)));
        });
    }

    @Test
    void platformRegistersControllerAndThreeGetEndpoints() {
        this.contextRunner.withPropertyValues("eva.mode=platform").run(context -> {
            assertEquals(1, context.getBeansOfType(PlatformTenantInspectCtrl.class).size());
            assertEquals(1, context.getBeansOfType(PlatformTenantInspectService.class).size());
            assertEquals(Set.of(
                    "GET /sys/platform/tenants/options",
                    "GET /sys/platform/tenants/{tenantId}/organizations",
                    "GET /sys/platform/tenants/{tenantId}/roles"),
                    platformMappings(context.getBean(RequestMappingHandlerMapping.class)));
        });
    }

    private Set<String> platformMappings(RequestMappingHandlerMapping mapping) {
        return mapping.getHandlerMethods().entrySet().stream()
                .filter(entry -> entry.getValue().getBeanType() == PlatformTenantInspectCtrl.class)
                .flatMap(entry -> entry.getKey().getPatternValues().stream()
                        .flatMap(path -> entry.getKey().getMethodsCondition().getMethods().stream()
                                .map(method -> method.name() + " " + path)))
                .collect(Collectors.toSet());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({PlatformTenantInspectCtrl.class, PlatformTenantInspectService.class})
    static class TestConfiguration {
        @Bean
        EvaConfig evaConfig() {
            return new EvaConfig();
        }

        @Bean
        I18NHelper i18NHelper() {
            return mock(I18NHelper.class);
        }

        @Bean
        TenantMapper tenantMapper() {
            return mock(TenantMapper.class);
        }

        @Bean
        OrganizationMapper organizationMapper() {
            return mock(OrganizationMapper.class);
        }

        @Bean
        OrganizationConvert organizationConvert() {
            return Mappers.getMapper(OrganizationConvert.class);
        }

        @Bean
        RoleMapper roleMapper() {
            return mock(RoleMapper.class);
        }

        @Bean
        RoleConvert roleConvert() {
            return Mappers.getMapper(RoleConvert.class);
        }

        @Bean
        TargetTenantExecutor targetTenantExecutor() {
            return mock(TargetTenantExecutor.class);
        }
    }
}
