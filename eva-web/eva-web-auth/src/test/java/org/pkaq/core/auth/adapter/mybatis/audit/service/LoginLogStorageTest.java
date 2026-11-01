package org.pkaq.core.auth.adapter.mybatis.audit.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.adapter.mybatis.audit.mapper.LoginLogMapper;
import org.pkaq.core.auth.adapter.mybatis.audit.mapper.LoginLogSqlProvider;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.web.core.client.ClientInfoResolver;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 使用真实 Spring 事务代理和 core 执行器、合成 JDBC 验证源与提交边界。
 * @author PKAQ
 * @date 2026-10-30
 */
class LoginLogStorageTest {
    private final EvaConfig config = new EvaConfig();
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final AuditTransactions transactions = new AuditTransactions();
    private String searchPath = "tenant_outer,eva_core";
    private String relationOverride;
    private LoginLogStorage storage;

    @BeforeEach
    void initialize() {
        config.setMode("platform");
        config.getTenant().setEnable(true);
        config.getTenant().setMode("schema");
        when(jdbc.queryForObject("SELECT current_setting('search_path')", String.class))
                .thenAnswer(invocation -> searchPath);
        when(jdbc.queryForObject(eq("SELECT pg_catalog.set_config('search_path', ?, true)"),
                eq(String.class), anyString())).thenAnswer(invocation -> {
                    searchPath = invocation.getArgument(2);
                    return searchPath;
                });
        when(jdbc.queryForMap(anyString(), anyString())).thenAnswer(invocation -> Map.of(
                "current_schema_name", searchPath.split(",")[0],
                "relation_schema_name", relationOverride == null ? searchPath.split(",")[0] : relationOverride));
        LoginLogStorage target = new LoginLogStorage(config, new CoreSchemaExecutor(jdbc, config), jdbc);
        ProxyFactory factory = new ProxyFactory(target);
        TransactionInterceptor advice = new TransactionInterceptor();
        advice.setTransactionManager(transactions);
        advice.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        factory.addAdvice(advice);
        storage = (LoginLogStorage) factory.getProxy();
    }

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @Test
    void coreWriteAndRiskUseOneNewTransactionAndRestoreOuterContext() {
        TenantContext.bind(23L, "tenant_outer");
        storage.write(() -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            assertEquals("eva_core", searchPath);
            assertEquals(0L, TenantContext.tenantId());
            assertTrue(LoginLogSqlProvider.riskCount().contains("TENANT_ID = #{tenantId}"));
            assertTrue(LoginLogSqlProvider.riskLast().contains("USER_ID = #{userId}"));
            assertThrows(RuntimeException.class, LoginLogSqlProvider::detail);
        });
        assertEquals(1, transactions.commits);
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW, transactions.propagation);
        assertEquals("tenant_outer,eva_core", searchPath);
        assertEquals(23L, TenantContext.tenantId());
        assertEquals("tenant_outer", TenantContext.schemaName());
        assertThrows(RuntimeException.class, LoginLogSqlProvider::riskCount);
    }

    @Test
    void readonlySourceAndExceptionalActionRestoreEverything() {
        assertThrows(IllegalStateException.class, () -> storage.read(() -> {
            assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            assertEquals("eva_core", searchPath);
            throw new IllegalStateException("synthetic action failure");
        }));
        assertEquals(1, transactions.rollbacks);
        assertEquals("tenant_outer,eva_core", searchPath);
        assertNull(TenantContext.tenantId());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    void physicalFallbackAndMissingRelationAreRejectedBeforeAction() {
        relationOverride = "pg_temp_3";
        assertThrows(RuntimeException.class, () -> storage.write(() -> {
            throw new AssertionError("不得执行写入");
        }));
        assertEquals("tenant_outer,eva_core", searchPath);
        assertNull(TenantContext.schemaName());
    }

    @Test
    void standaloneKeepsDefaultSourceAndNeverRedirectsIt() {
        config.setMode("standalone");
        config.getTenant().setEnable(false);
        searchPath = "eva";
        storage.write(() -> assertEquals("eva", searchPath));
        searchPath = "public";
        assertThrows(RuntimeException.class, () -> storage.read(() -> "must not run"));
        assertEquals("public", searchPath);
    }

    @Test
    void actualProxyCommitFailureIsCaughtOutsideTransactionByFacade() {
        transactions.failCommit = true;
        LoginLogMapper mapper = mock(LoginLogMapper.class);
        LoginLogService service = new LoginLogService(mapper, new ClientInfoResolver(config), storage,
                mock(LoginLogReadAccess.class));
        assertDoesNotThrow(() -> service.saveFailure(new MockHttpServletRequest(), "synthetic"));
        verify(mapper).insert(any(org.pkaq.core.auth.adapter.mybatis.audit.entity.LoginLogEntity.class));
        assertEquals(1, transactions.commits);
        assertEquals("tenant_outer,eva_core", searchPath);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertThrows(RuntimeException.class, LoginLogSqlProvider::riskCount);
    }

    @Test
    void auditCommitSurvivesCallerRollbackAndResumesCallerTransaction() {
        new TransactionTemplate(transactions).execute(status -> {
            Object caller = transactions.current.get();
            storage.write(() -> {
                assertFalse(caller == transactions.current.get());
                assertEquals("eva_core", searchPath);
            });
            assertTrue(caller == transactions.current.get());
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            status.setRollbackOnly();
            return null;
        });
        assertEquals(1, transactions.commits);
        assertEquals(1, transactions.rollbacks);
        assertNull(transactions.current.get());
    }

    private static final class AuditTransactions extends AbstractPlatformTransactionManager {
        private final ThreadLocal<Object> current = new ThreadLocal<>();
        private int propagation;
        private int commits;
        private int rollbacks;
        private boolean failCommit;

        @Override
        protected Object doGetTransaction() {
            return new State(current.get());
        }

        @Override
        protected boolean isExistingTransaction(Object transaction) {
            return ((State) transaction).token != null;
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            propagation = definition.getPropagationBehavior();
            State state = (State) transaction;
            state.token = new Object();
            current.set(state.token);
        }

        @Override
        protected Object doSuspend(Object transaction) {
            State state = (State) transaction;
            Object previous = state.token;
            state.token = null;
            current.remove();
            return previous;
        }

        @Override
        protected void doResume(Object transaction, Object suspended) {
            ((State) transaction).token = suspended;
            current.set(suspended);
        }

        @Override
        protected void doCleanupAfterCompletion(Object transaction) {
            current.remove();
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            commits++;
            if (failCommit) {
                throw new TransactionSystemException("synthetic commit failure");
            }
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            rollbacks++;
        }

        private static final class State {
            private Object token;

            private State(Object token) {
                this.token = token;
            }
        }
    }
}
