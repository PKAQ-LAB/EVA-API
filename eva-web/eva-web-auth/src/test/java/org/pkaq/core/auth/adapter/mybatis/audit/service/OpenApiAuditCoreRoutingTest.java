package org.pkaq.core.auth.adapter.mybatis.audit.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pkaq.core.auth.authorization.service.ResourceAuthorizationService;
import org.pkaq.core.auth.spi.IResourcePermissionQuery;
import org.pkaq.core.mybatis.log.OpenApiAuditQueryScope;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 真实只读事务代理及core执行器的路由恢复测试，JDBC与连接均为替身。
 * @author PKAQ
 * @date 2026-10-24
 */
class OpenApiAuditCoreRoutingTest {
    private final EvaConfig config = new EvaConfig();
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final Connection connection = mock(Connection.class);
    private final AtomicReference<String> searchPath = new AtomicReference<>("outer_schema");
    private final AtomicReference<String> authoritySql = new AtomicReference<>();

    @AfterEach
    void clearContext() {
        TenantContext.clear();
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    void effectiveCoreAdminReadsInExplicitCoreReadOnlyTransaction() throws Exception {
        OpenApiAuditReadAccess proxy = proxy(true, false);
        TenantContext.bind(0L, "outer_schema");
        ThreadUserHelper.runWithUser(user(), () -> assertEquals("result",
                proxy.read("/monitor/log/openapi/list", () -> {
            assertCoreTransaction();
            OpenApiAuditQueryScope.requireAuthorized(
                    "org.pkaq.core.auth.adapter.mybatis.audit.mapper.OpenApiCallLogMapper.selectList_mpCount");
            return "result";
        })));
        assertEquals("outer_schema", searchPath.get());
        assertEquals("outer_schema", TenantContext.schemaName());
        verify(connection).setReadOnly(true);
        verify(connection).commit();
        assertAuthoritySql();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void missingEffectiveRoleOrAuthorityQueryFailureNeverRunsLogAction(boolean queryFailure) throws Exception {
        OpenApiAuditReadAccess proxy = proxy(false, queryFailure);
        TenantContext.bind(0L, "outer_schema");
        AtomicBoolean called = new AtomicBoolean();
        ThreadUserHelper.runWithUser(user(), () -> assertThrows(RuntimeException.class,
                () -> proxy.read("/monitor/log/openapi/list", () -> called.getAndSet(true))));
        assertFalse(called.get());
        assertEquals("outer_schema", searchPath.get());
        assertEquals("outer_schema", TenantContext.schemaName());
        verify(connection).rollback();
        assertAuthoritySql();
    }

    @Test
    void logFailureRestoresSearchPathContextAndReadScope() throws Exception {
        OpenApiAuditReadAccess proxy = proxy(true, false);
        ThreadUserHelper.runWithUser(user(), () -> {
            assertThrows(IllegalArgumentException.class, () -> proxy.read("/monitor/log/openapi/list", () -> {
                assertCoreTransaction();
                throw new IllegalArgumentException("模拟日志失败");
            }));
            assertThrows(RuntimeException.class, () -> OpenApiAuditQueryScope.requireAuthorized(
                    "org.pkaq.core.auth.adapter.mybatis.audit.mapper.OpenApiCallLogMapper.selectById"));
        });
        assertEquals("outer_schema", searchPath.get());
        assertEquals(null, TenantContext.schemaName());
        verify(connection).rollback();
    }

    @Test
    void routingFailureRestoresPreviousSchemaBeforeAuthorityQuery() throws Exception {
        OpenApiAuditReadAccess proxy = proxy(true, false);
        doAnswer(invocation -> {
                    searchPath.set(config.getTenant().getCoreSchema());
                    throw new IllegalStateException("模拟路由失败");
                }).when(jdbc).queryForObject("SELECT pg_catalog.set_config('search_path', ?, true)",
                        String.class, config.getTenant().getCoreSchema());
        TenantContext.bind(0L, "outer_schema");
        ThreadUserHelper.runWithUser(user(), () -> assertThrows(IllegalStateException.class,
                () -> proxy.read("/monitor/log/openapi/list", () -> {
                    throw new AssertionError("路由失败后不应执行读取");
                })));
        assertEquals(null, authoritySql.get());
        assertEquals("outer_schema", searchPath.get());
        assertEquals("outer_schema", TenantContext.schemaName());
    }

    @Test
    void restoreFailureStillRestoresThreadContextAndReadWindow() throws Exception {
        OpenApiAuditReadAccess proxy = proxy(true, false);
        doThrow(new IllegalStateException("模拟恢复失败")).when(jdbc)
                .queryForObject("SELECT pg_catalog.set_config('search_path', ?, true)", String.class, "outer_schema");
        TenantContext.bind(0L, "outer_schema");
        ThreadUserHelper.runWithUser(user(), () -> {
            assertThrows(IllegalStateException.class,
                    () -> proxy.read("/monitor/log/openapi/list", () -> "result"));
            assertThrows(RuntimeException.class, () -> OpenApiAuditQueryScope.requireAuthorized(
                    "org.pkaq.core.auth.adapter.mybatis.audit.mapper.OpenApiCallLogMapper.selectById"));
        });
        assertEquals("outer_schema", TenantContext.schemaName());
        verify(connection).rollback();
    }

    private OpenApiAuditReadAccess proxy(boolean eligible, boolean queryFailure) throws Exception {
        config.setMode("platform");
        config.getTenant().setEnable(true);
        config.getTenant().setMode("schema");
        config.getResourcePermission().setEnable(false);
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(jdbc.queryForObject("SELECT current_setting('search_path')", String.class))
                .thenAnswer(invocation -> searchPath.get());
        when(jdbc.queryForObject(eq("SELECT pg_catalog.set_config('search_path', ?, true)"),
                eq(String.class), anyString())).thenAnswer(invocation -> {
                    searchPath.set(invocation.getArgument(2));
                    return searchPath.get();
                });
        when(jdbc.queryForObject(anyString(), eq(Boolean.class), eq(7L), eq("ROLE_ADMIN")))
                .thenAnswer(invocation -> {
                    authoritySql.set(invocation.getArgument(0));
                    assertCoreTransaction();
                    if (queryFailure) {
                        throw new IllegalStateException("模拟资格查询失败");
                    }
                    return eligible;
                });
        OpenApiAuditReadAccess service = new OpenApiAuditReadAccess(config,
                new OpenApiAuditAdministratorQuery(jdbc),
                new ResourceAuthorizationService(config, mock(IResourcePermissionQuery.class)),
                new CoreSchemaExecutor(jdbc, config));
        TransactionInterceptor transaction = new TransactionInterceptor();
        transaction.setTransactionManager(new DataSourceTransactionManager(dataSource));
        transaction.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        AspectJProxyFactory factory = new AspectJProxyFactory(service);
        factory.setProxyTargetClass(true);
        factory.addAdvice(transaction);
        return factory.getProxy();
    }

    private void assertAuthoritySql() {
        String sql = authoritySql.get();
        assertTrue(sql.contains("account.DELETED = 0"));
        assertTrue(sql.contains("role.DELETED = 0"));
        assertTrue(sql.contains("account.FROZEN IN (0, 9999)"));
        assertTrue(sql.contains("role.FROZEN IN (0, 9999)"));
        assertTrue(sql.contains("account.TENANT_ID = 0"));
        assertTrue(sql.contains("role.TENANT_ID = 0"));
        assertTrue(sql.contains("membership.USER_ID = account.ID"));
        assertTrue(sql.contains("role.ID = membership.ROLE_ID"));
        assertFalse(sql.contains("membership.DELETED"));
    }

    private void assertCoreTransaction() {
        assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
        assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        assertEquals(config.getTenant().getCoreSchema(), searchPath.get());
        assertEquals(config.getTenant().getCoreSchema(), TenantContext.schemaName());
        assertEquals(0L, TenantContext.tenantId());
    }

    private ThreadUser user() {
        return new ThreadUser().setUserId(7L).setTenantId(0L).setDataScopes(List.of());
    }
}
