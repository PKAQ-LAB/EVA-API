package org.pkaq.core.auth.adapter.mybatis.audit.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.pkaq.core.auth.adapter.mybatis.audit.entity.OpenApiCallLogEntity;
import org.pkaq.core.auth.adapter.mybatis.audit.mapper.OpenApiCallLogMapper;
import org.pkaq.core.auth.authorization.service.ResourceAuthorizationService;
import org.pkaq.core.auth.spi.IOpenApiAuditAdministratorQuery;
import org.pkaq.core.auth.spi.IResourcePermissionQuery;
import org.pkaq.config.MybatisPlusConfig;
import org.pkaq.core.mybatis.log.OpenApiAuditQueryScope;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.web.core.client.ClientInfoResolver;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 集中审计常驻资格、资源开关与写入边界的隔离回归。
 * @author PKAQ
 * @date 2026-10-24
 */
class OpenApiAuditReadAccessTest {
    private final EvaConfig config = new EvaConfig();
    private final IOpenApiAuditAdministratorQuery administrators = mock(IOpenApiAuditAdministratorQuery.class);
    private final IResourcePermissionQuery resources = mock(IResourcePermissionQuery.class);
    private final CoreSchemaExecutor core = mock(CoreSchemaExecutor.class);
    private final OpenApiCallLogMapper mapper = mock(OpenApiCallLogMapper.class);
    private final OpenApiAuditReadAccess access = new OpenApiAuditReadAccess(config, administrators,
            new ResourceAuthorizationService(config, resources), core);
    private final OpenApiCallLogService service = new OpenApiCallLogService(mapper,
            new ClientInfoResolver(config), access);
    private static final String STATEMENT =
            "org.pkaq.core.auth.adapter.mybatis.audit.mapper.OpenApiCallLogMapper.selectById";

    @BeforeEach
    void initializeEntity() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                OpenApiCallLogEntity.class);
        config.getTenant().setMode("standalone");
        config.getAuth().setPermit(new String[0]);
    }

    @AfterEach
    void clearContext() {
        TenantContext.clear();
    }

    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    void effectiveAdminReadsDetailAndPageRegardlessOfDataSwitch(boolean data, boolean resource) {
        config.getDataPermission().setEnable(data);
        config.getResourcePermission().setEnable(resource);
        when(administrators.isActiveAdministrator(7L)).thenReturn(true);
        if (resource) {
            when(resources.hasPermission(eq(0L), eq(List.of(8L)), eq("GET"), anyString())).thenReturn(true);
        }
        doAnswer(invocation -> {
            OpenApiAuditQueryScope.requireAuthorized(STATEMENT);
            assertEquals(0L, TenantContext.tenantId());
            return null;
        }).when(mapper).selectById(3L);
        when(mapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            OpenApiAuditQueryScope.requireAuthorized(STATEMENT.replace("selectById", "selectList_mpCount"));
            return new Page<OpenApiCallLogEntity>();
        });
        ThreadUser user = user(0L, resource ? Map.of(8L, "ROLE_ADMIN") : Map.of());
        ThreadUserHelper.runWithUser(user, () -> {
            assertNull(service.get(3L));
            assertEquals(0L, service.list(null).getTotal());
            assertThrows(RuntimeException.class, () -> OpenApiAuditQueryScope.requireAuthorized(STATEMENT));
        });
        assertNull(TenantContext.tenantId());
        verifyNoInteractions(core);
    }

    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    void switchesNeverAllowInactiveOrMissingAdmin(boolean data, boolean resource) {
        config.getDataPermission().setEnable(data);
        config.getResourcePermission().setEnable(resource);
        ThreadUserHelper.runWithUser(user(0L, Map.of(8L, "ROLE_ADMIN")), () -> {
            assertThrows(RuntimeException.class, () -> service.get(3L));
            assertThrows(RuntimeException.class, () -> service.list(null));
        });
        verifyNoInteractions(mapper, resources, core);
    }

    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    void realBeanConfigurationNeverMakesGuardDependOnDataInterceptor(boolean data, boolean resource) {
        config.getDataPermission().setEnable(data);
        config.getResourcePermission().setEnable(resource);
        PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
        when(transactions.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(EvaConfig.class, () -> config);
            context.registerBean(PlatformTransactionManager.class, () -> transactions);
            context.registerBean(OpenApiAuditReadAccess.class, () -> access);
            context.register(MybatisPlusConfig.class);
            context.refresh();
            MybatisPlusInterceptor plugins = context.getBean(MybatisPlusInterceptor.class);
            assertEquals(data, plugins.getInterceptors().stream()
                    .anyMatch(DataPermissionInterceptor.class::isInstance));
            OpenApiAuditReadAccess bean = context.getBean(OpenApiAuditReadAccess.class);
            ThreadUserHelper.runWithUser(user(0L, Map.of()), () ->
                    assertThrows(RuntimeException.class, () -> bean.read("/monitor/log/openapi/list", () -> {
                        throw new AssertionError("资格拒绝后不应读取日志");
                    })));
            assertFalse(plugins.getInterceptors().isEmpty());
        }
        verifyNoInteractions(mapper, resources, core);
    }

    @ParameterizedTest
    @CsvSource({"ROLE_ADMIN", "TENANT_ADMIN", "TENANT_INSPECT", "API_USER"})
    void positiveTenantAndAllScopeNeverReachCoreOrLogMapper(String role) {
        config.setMode("platform");
        config.getTenant().setEnable(true);
        config.getTenant().setMode("schema");
        ThreadUser user = user(12L, Map.of(8L, role));
        user.setDataScopes(List.of(new ThreadUser.DataScope("0000", List.of())));
        ThreadUserHelper.runWithUser(user, () -> {
            assertThrows(RuntimeException.class, () -> service.get(3L));
            assertThrows(RuntimeException.class, () -> service.list(null));
        });
        verifyNoInteractions(administrators, resources, mapper, core);
    }

    @Test
    void missingIdentityAndUnsupportedModesFailBeforeAuthorityQuery() {
        assertThrows(RuntimeException.class, () -> service.get(3L));
        ThreadUserHelper.runWithUser(user(0L, Map.of()).setUserId(0L), () ->
                assertThrows(RuntimeException.class, () -> service.get(3L)));
        config.setMode("saas");
        ThreadUserHelper.runWithUser(user(0L, Map.of()), () ->
                assertThrows(RuntimeException.class, () -> service.get(3L)));
        verifyNoInteractions(administrators, mapper, core);
    }

    @Test
    void resourceOnStillRequiresExistingRbacAfterAdminQualification() {
        config.getResourcePermission().setEnable(true);
        when(administrators.isActiveAdministrator(7L)).thenReturn(true);
        ThreadUserHelper.runWithUser(user(0L, Map.of(8L, "ROLE_ADMIN")), () -> {
            assertThrows(RuntimeException.class, () -> service.get(3L));
            assertThrows(RuntimeException.class, () -> service.list(null));
        });
        verifyNoInteractions(mapper);
        verify(resources).hasPermission(0L, List.of(8L), "GET", "/monitor/log/openapi/get/3");
        verify(resources).hasPermission(0L, List.of(8L), "GET", "/monitor/log/openapi/list");
    }

    @Test
    void qualificationFailureRestoresPreviousContextAndNeverReadsLogs() {
        when(administrators.isActiveAdministrator(7L)).thenThrow(new IllegalStateException("模拟查询失败"));
        TenantContext.bind(0L, config.getTenant().getStandaloneSchema());
        ThreadUserHelper.runWithUser(user(0L, Map.of()), () ->
                assertThrows(IllegalStateException.class, () -> service.get(3L)));
        assertEquals(0L, TenantContext.tenantId());
        assertEquals(config.getTenant().getStandaloneSchema(), TenantContext.schemaName());
        verifyNoInteractions(mapper, resources);
    }

    @Test
    void standaloneRejectsConflictingSchemaContext() {
        TenantContext.bind(12L, "tenant_12");
        ThreadUserHelper.runWithUser(user(0L, Map.of()), () ->
                assertThrows(RuntimeException.class, () -> service.get(3L)));
        assertEquals("tenant_12", TenantContext.schemaName());
        verifyNoInteractions(administrators, mapper);
    }

    @Test
    void unqualifiedAuditWriteAndFailedWriteRemainIndependentOfReadGuard() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/demo");
        service.save(request, null, "/openapi/demo", System.nanoTime(), 403, "拒绝访问");
        verify(mapper).insert(any(OpenApiCallLogEntity.class));
        verifyNoInteractions(administrators, resources, core);
        verify(mapper, never()).selectById(any(Long.class));
    }

    @Test
    void failedAuditInsertStillDoesNotBreakAnonymousCaller() {
        doThrow(new IllegalStateException("模拟存储失败")).when(mapper).insert(any(OpenApiCallLogEntity.class));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/demo");
        assertDoesNotThrow(() -> service.save(request, null, "/openapi/demo", System.nanoTime(), 401, "签名失败"));
        verifyNoInteractions(administrators, resources, core);
    }

    private ThreadUser user(long tenant, Map<Long, String> roles) {
        Map<Long, ThreadUser.GrantedRoles> granted = new java.util.HashMap<>();
        roles.forEach((id, code) -> granted.put(id, new ThreadUser.GrantedRoles(code, code)));
        return new ThreadUser().setUserId(7L).setTenantId(tenant).setRolesMap(granted).setDataScopes(List.of());
    }
}
