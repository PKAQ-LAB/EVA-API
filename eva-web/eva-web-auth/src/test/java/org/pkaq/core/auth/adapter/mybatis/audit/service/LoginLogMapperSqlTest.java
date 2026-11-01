package org.pkaq.core.auth.adapter.mybatis.audit.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.sf.jsqlparser.schema.Table;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pkaq.config.MybatisPlusDataPermissionHandler;
import org.pkaq.core.auth.adapter.mybatis.audit.entity.LoginLogEntity;
import org.pkaq.core.auth.adapter.mybatis.audit.mapper.LoginLogMapper;
import org.pkaq.core.auth.audit.bo.LoginLogQueryBo;
import org.pkaq.core.mybatis.log.LoginLogQueryScope;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 实际 MyBatis ProviderSqlSource 与 MP 自动 count 的离线验证。
 * @author PKAQ
 * @date 2026-10-30
 */
class LoginLogMapperSqlTest {
    private static final String MAPPER = LoginLogMapper.class.getName() + ".";
    private final MybatisConfiguration configuration = new MybatisConfiguration();
    private final ThreadUser user = new ThreadUser().setUserId(7L).setTenantId(23L);

    @BeforeEach
    void initialize() {
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setEnvironment(new Environment("synthetic", new JdbcTransactionFactory(), mock(DataSource.class)));
        configuration.addMapper(LoginLogMapper.class);
        new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    void everyBoundSqlInvocationRechecksScopeEvenWithNoDataPlugin() {
        MappedStatement detail = configuration.getMappedStatement(MAPPER + "selectHistoryDetail");
        assertEquals(LoginLogEntity.class, detail.getResultMaps().getFirst().getType());
        assertThrows(RuntimeException.class, () -> detail.getBoundSql(Map.of("id", 3L)));
        ThreadUserHelper.runWithUser(user, () -> LoginLogQueryScope.read(23L, () -> {
            String sql = detail.getBoundSql(Map.of("id", 3L)).getSql();
            assertTrue(sql.contains("TENANT_ID = 23"));
            return null;
        }));
        assertThrows(RuntimeException.class, () -> detail.getBoundSql(Map.of("id", 3L)));
    }

    @Test
    void actualPaginationCountAndRecordsKeepTheSameImmutableTenantPredicate() {
        MappedStatement statement = configuration.getMappedStatement(MAPPER + "selectHistoryPage");
        LoginLogQueryBo query = new LoginLogQueryBo();
        query.setTargetTenantId(999L);
        query.setAccount("synthetic");
        Page<LoginLogEntity> page = new Page<>(1, 10);
        Map<String, Object> parameters = new HashMap<>(Map.of("page", page, "query", query));
        Executor executor = mock(Executor.class);
        ThreadUserHelper.runWithUser(user, () -> LoginLogQueryScope.read(23L, () -> {
            BoundSql original = statement.getBoundSql(parameters);
            try {
                when(executor.query(any(), eq(parameters), eq(RowBounds.DEFAULT), any(), any(), any()))
                        .thenAnswer(invocation -> {
                            MappedStatement countStatement = invocation.getArgument(0);
                            assertEquals(MAPPER + "selectHistoryPage_mpCount", countStatement.getId());
                            BoundSql count = invocation.getArgument(5);
                            assertTrue(count.getSql().toUpperCase().contains("COUNT"));
                            assertTrue(count.getSql().contains("TENANT_ID = 23"));
                            assertFalse(count.getSql().contains("999"));
                            return List.of(2L);
                        });
                assertTrue(new PaginationInnerInterceptor().willDoQuery(executor, statement, parameters,
                        RowBounds.DEFAULT, null, original));
                assertEquals(2L, page.getTotal());
                assertTrue(original.getSql().contains("TENANT_ID = 23"));
            } catch (java.sql.SQLException exception) {
                throw new AssertionError(exception);
            }
            return null;
        }));
        assertThrows(RuntimeException.class, () -> statement.getBoundSql(parameters));
        // MP 的派生 count 只在插件缓存中，不注册为可直接调用的 Mapper 语句。
        assertFalse(configuration.hasStatement(MAPPER + "selectHistoryPage_mpCount"));
    }

    @Test
    void ownerCompatibilityIsLimitedToExactMethodsAndMatchingWindow() {
        EvaConfig config = new EvaConfig();
        config.getDataPermission().setEnable(true);
        MybatisPlusDataPermissionHandler handler = new MybatisPlusDataPermissionHandler(config);
        assertThrows(RuntimeException.class,
                () -> handler.getSqlSegment(new Table("SYS_LOGIN_LOG"), null, MAPPER + "selectHistoryDetail"));
        ThreadUserHelper.runWithUser(user, () -> LoginLogQueryScope.read(23L, () -> {
            assertNull(handler.getSqlSegment(new Table("SYS_LOGIN_LOG"), null, MAPPER + "selectHistoryDetail"));
            assertNull(handler.getSqlSegment(new Table("SYS_LOGIN_LOG"), null, MAPPER + "selectHistoryPage_mpCount"));
            assertThrows(RuntimeException.class,
                    () -> handler.getSqlSegment(new Table("SYS_LOGIN_LOG"), null, MAPPER + "selectRiskLastLogin"));
            return null;
        }));
        LoginLogQueryScope.write(() -> {
            assertNull(handler.getSqlSegment(new Table("SYS_LOGIN_LOG"), null, MAPPER + "selectRiskDeviceCount"));
            assertNull(handler.getSqlSegment(new Table("SYS_LOGIN_LOG"), null, MAPPER + "update"));
            assertThrows(RuntimeException.class,
                    () -> handler.getSqlSegment(new Table("SYS_LOGIN_LOG"), null, MAPPER + "selectHistoryDetail"));
        });
    }

    @Test
    void nestedFailureRestoresScopeAndChangedIdentityCannotReuseIt() {
        ThreadUserHelper.runWithUser(user, () -> LoginLogQueryScope.read(23L, () -> {
            assertThrows(IllegalStateException.class, () -> LoginLogQueryScope.read(23L, () -> {
                throw new IllegalStateException("synthetic");
            }));
            assertEquals(23L, LoginLogQueryScope.requireHistory(MAPPER + "selectHistoryDetail"));
            user.setTenantId(24L);
            assertThrows(RuntimeException.class,
                    () -> LoginLogQueryScope.requireHistory(MAPPER + "selectHistoryDetail"));
            return null;
        }));
        assertThrows(RuntimeException.class, () -> LoginLogQueryScope.requireHistory(MAPPER + "selectHistoryDetail"));
    }
}
