package org.pkaq.core.auth.adapter.mybatis.audit.service;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;
import org.pkaq.config.MybatisPlusDataPermissionHandler;
import org.pkaq.core.auth.adapter.mybatis.audit.entity.OpenApiCallLogEntity;
import org.pkaq.core.auth.adapter.mybatis.audit.mapper.OpenApiCallLogMapper;
import org.pkaq.core.mybatis.log.OpenApiAuditQueryScope;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 使用真实BaseMapper注入SQL与分页插件验证计数同源，无数据库连接。
 * @author PKAQ
 * @date 2026-10-24
 */
class OpenApiAuditMapperSqlTest {
    @Test
    void realMapperDetailPageAndGeneratedCountShareOnlyCentralAuditTable() throws Exception {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMapper(OpenApiCallLogMapper.class);
        EvaConfig config = new EvaConfig();
        config.getDataPermission().setEnable(true);
        DataPermissionInterceptor data = new DataPermissionInterceptor(new MybatisPlusDataPermissionHandler(config));
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        Executor executor = mock(Executor.class);
        AtomicReference<String> countSql = new AtomicReference<>();
        when(executor.createCacheKey(any(), any(), any(), any())).thenReturn(new CacheKey());
        when(executor.query(any(MappedStatement.class), any(), any(RowBounds.class), isNull(),
                any(CacheKey.class), any(BoundSql.class))).thenAnswer(invocation -> {
                    MappedStatement count = invocation.getArgument(0);
                    assertTrue(count.getId().endsWith("selectList_mpCount"));
                    countSql.set(((BoundSql) invocation.getArgument(5)).getSql());
                    return List.of(1L);
                });
        String mapper = OpenApiCallLogMapper.class.getName() + ".";
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setTenantId(0L).setDataScopes(List.of()), () -> {
            for (String method : List.of("selectById", "selectList", "selectCount")) {
                MappedStatement statement = configuration.getMappedStatement(mapper + method);
                Map<String, Object> parameters = new java.util.HashMap<>();
                parameters.put("id", 3L);
                parameters.put("ew", Wrappers.<OpenApiCallLogEntity>lambdaQuery());
                parameters.put("page", new Page<>(1, 10));
                assertThrows(RuntimeException.class, () ->
                        data.beforeQuery(executor, statement, parameters, RowBounds.DEFAULT, null,
                                statement.getBoundSql(parameters)));
                OpenApiAuditQueryScope.read(() -> {
                    BoundSql sql = statement.getBoundSql(parameters);
                    try {
                        data.beforeQuery(executor, statement, parameters, RowBounds.DEFAULT, null, sql);
                        if ("selectList".equals(method)) {
                            assertTrue(pagination.willDoQuery(executor, statement, parameters,
                                    RowBounds.DEFAULT, null, sql));
                            assertCentralSql(countSql.get());
                        }
                    } catch (java.sql.SQLException exception) {
                        throw new AssertionError(exception);
                    }
                    assertCentralSql(sql.getSql());
                    return null;
                });
            }
        });
    }

    private void assertCentralSql(String sql) {
        assertTrue(sql.contains("API_CALL_LOG"));
        assertFalse(sql.contains("CREATE_ID ="));
        assertFalse(sql.contains("1 = 0"));
        assertFalse(sql.contains("TENANT_ID ="));
    }
}
