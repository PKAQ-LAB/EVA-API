package org.pkaq.config;

import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;
import org.pkaq.core.mybatis.log.OpenApiAuditQueryScope;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 集中审计读取窗口仅豁免指定Mapper，不能传播到其他表或身份。
 * @author PKAQ
 * @date 2026-10-24
 */
class OpenApiAuditQueryPermissionTest {
    private static final String MAPPER = "org.pkaq.core.auth.adapter.mybatis.audit.mapper.OpenApiCallLogMapper.";
    private final EvaConfig config = new EvaConfig();
    private final DataPermissionInterceptor interceptor =
            new DataPermissionInterceptor(new MybatisPlusDataPermissionHandler(config));

    @Test
    void detailListAndPaginationCountRequireWindowEvenWithEmptyScopes() {
        for (String method : List.of("selectById", "selectList", "selectCount", "selectList_mpCount",
                "selectPage_mpCount")) {
            assertThrows(RuntimeException.class, () -> intercept(MAPPER + method, "API_CALL_LOG"));
            ThreadUserHelper.runWithUser(user(), () -> {
                assertThrows(RuntimeException.class, () -> intercept(MAPPER + method, "API_CALL_LOG"));
                OpenApiAuditQueryScope.read(() -> {
                    String sql = intercept(MAPPER + method, "API_CALL_LOG");
                    assertFalse(sql.contains("CREATE_ID"));
                    assertFalse(sql.contains("1 = 0"));
                    return null;
                });
                assertThrows(RuntimeException.class, () -> intercept(MAPPER + method, "API_CALL_LOG"));
            });
        }
    }

    @Test
    void unrelatedMapperAndOtherTablesCannotUseAuditExemption() {
        config.getDataPermission().setEnable(true);
        ThreadUserHelper.runWithUser(user(), () -> OpenApiAuditQueryScope.read(() -> {
            assertThrows(RuntimeException.class, () -> intercept("other.Mapper.selectList", "API_CALL_LOG"));
            assertThrows(RuntimeException.class, () -> intercept(MAPPER + "customRead", "API_CALL_LOG"));
            assertTrue(intercept(MAPPER + "selectList", "SYS_MODULE").contains("1 = 0"));
            assertTrue(intercept("other.Mapper.selectList", "SYS_MODULE").contains("1 = 0"));
            return null;
        }));
    }

    @Test
    void scopeDoesNotSurviveUserSwitchOrExceptionalExit() {
        ThreadUserHelper.runWithUser(user(), () -> {
            assertThrows(IllegalStateException.class, () -> OpenApiAuditQueryScope.read(() -> {
                ThreadUserHelper.runWithUser(user(), () -> assertThrows(RuntimeException.class,
                        () -> intercept(MAPPER + "selectById", "API_CALL_LOG")));
                OpenApiAuditQueryScope.read(() -> intercept(MAPPER + "selectById", "API_CALL_LOG"));
                intercept(MAPPER + "selectById", "API_CALL_LOG");
                throw new IllegalStateException("模拟读取失败");
            }));
            assertThrows(RuntimeException.class, () -> intercept(MAPPER + "selectById", "API_CALL_LOG"));
        });
    }

    @Test
    void anonymousAndPositiveTenantCannotOpenWindow() {
        assertThrows(RuntimeException.class, () -> OpenApiAuditQueryScope.read(() -> null));
        ThreadUserHelper.runWithUser(user().setTenantId(12L), () ->
                assertThrows(RuntimeException.class, () -> OpenApiAuditQueryScope.read(() -> null)));
    }

    private String intercept(String statementId, String table) {
        Configuration configuration = new Configuration();
        MappedStatement statement = new MappedStatement.Builder(configuration, statementId,
                new StaticSqlSource(configuration, "SELECT * FROM " + table), SqlCommandType.SELECT).build();
        BoundSql sql = statement.getBoundSql(null);
        try {
            interceptor.beforeQuery(null, statement, null, RowBounds.DEFAULT, null, sql);
        } catch (java.sql.SQLException exception) {
            throw new AssertionError(exception);
        }
        return sql.getSql();
    }

    private ThreadUser user() {
        return new ThreadUser().setUserId(7L).setTenantId(0L).setDataScopes(List.of());
    }
}
