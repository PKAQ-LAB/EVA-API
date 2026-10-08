package org.pkaq.core.auth.architecture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.adapter.tenant.TenantAuthRoutingService;
import org.pkaq.core.mybatis.tenant.TenantSchemaRouter;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.core.tenant.TenantContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 在隔离替身事务中验证嵌套租户路由恢复，不连接数据库。
 *
 * @author Codex
 * @date 2026-10-08
 */
class TenantRoutingBoundaryReviewTest {
    private final TenantSchemaRouter router = mock(TenantSchemaRouter.class);

    @AfterEach
    void clearContext() {
        TenantContext.clear();
    }

    /** 嵌套角色加载退出后必须恢复外层认证的租户上下文。 */
    @Test
    void shouldRestoreOuterContextAfterNestedLookup() {
        TenantAuthRoutingService service = service();
        service.execute(11L, () -> {
            assertEquals(11L, TenantContext.tenantId());
            service.execute(22L, () -> {
                assertEquals(22L, TenantContext.tenantId());
                return "roles";
            });
            assertEquals(11L, TenantContext.tenantId());
            assertEquals("tenant_11", TenantContext.schemaName());
            return "account";
        });
        assertNull(TenantContext.tenantId());
        assertNull(TenantContext.schemaName());
    }

    /** 查询抛错不得清空调用前已经绑定的租户。 */
    @Test
    void shouldRestoreContextWhenLookupFails() {
        TenantAuthRoutingService service = service();
        TenantContext.bind(7L, "tenant_7");
        assertThrows(IllegalArgumentException.class, () -> service.execute(11L, () -> {
            throw new IllegalArgumentException("lookup failed");
        }));
        assertEquals(7L, TenantContext.tenantId());
        assertEquals("tenant_7", TenantContext.schemaName());
    }

    /** 数据库 search_path 恢复失败时，线程上下文仍必须恢复。 */
    @Test
    void shouldRestoreContextWhenSearchPathRestoreFails() {
        TenantAuthRoutingService service = service();
        TenantContext.bind(7L, "tenant_7");
        doThrow(new IllegalStateException("restore failed")).when(router).restoreCurrentTransaction(any());
        assertThrows(IllegalStateException.class, () -> service.execute(11L, () -> "account"));
        assertEquals(7L, TenantContext.tenantId());
        assertEquals("tenant_7", TenantContext.schemaName());
    }

    /** 路由初始化失败时也不能遗留新绑定的租户。 */
    @Test
    void shouldRestoreContextWhenRouteFails() {
        TenantAuthRoutingService service = service();
        TenantContext.bind(7L, "tenant_7");
        doAnswer(invocation -> {
            TenantContext.bind(11L, "tenant_11");
            throw new IllegalStateException("route failed");
        }).when(router).routeCurrentTransaction(11L);
        assertThrows(IllegalStateException.class, () -> service.execute(11L, () -> "account"));
        assertEquals(7L, TenantContext.tenantId());
        assertEquals("tenant_7", TenantContext.schemaName());
    }

    private TenantAuthRoutingService service() {
        EvaConfig config = new EvaConfig();
        config.getTenant().setEnable(true);
        config.getTenant().setMode(TenantProperties.MODE_SCHEMA);
        TransactionTemplate transaction = mock(TransactionTemplate.class);
        when(transaction.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
        when(router.routeCurrentTransaction(anyLong())).thenAnswer(invocation -> {
            Long tenantId = invocation.getArgument(0);
            String previous = TenantContext.schemaName();
            TenantContext.bind(tenantId, "tenant_" + tenantId);
            return null == previous ? "public" : previous;
        });
        return new TenantAuthRoutingService(transaction, router, config);
    }
}
