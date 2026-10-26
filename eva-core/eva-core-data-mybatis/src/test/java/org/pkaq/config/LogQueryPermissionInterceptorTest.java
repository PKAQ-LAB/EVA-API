package org.pkaq.config;

import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 真实分页数据权限拦截器与Mapper SQL的日志查询守卫回归。
 * @author PKAQ
 * @date 2026-10-23
 */
class LogQueryPermissionInterceptorTest {
    private final EvaConfig config = new EvaConfig();
    private final DataPermissionInterceptor interceptor =
            new DataPermissionInterceptor(new MybatisPlusDataPermissionHandler(config));

    @Test
    void guardsRealHotArchiveCountAndDetailSqlBeforeDatabaseAccess() throws Exception {
        Configuration configuration = new com.baomidou.mybatisplus.core.MybatisConfiguration();
        try (InputStream input = getClass().getResourceAsStream("/mapper/BusinessLogMapper.xml")) {
            new XMLMapperBuilder(input, configuration, "mapper/BusinessLogMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        for (String method : List.of("selectLogById", "countLogs", "selectLogPage")) {
            MappedStatement statement = configuration.getMappedStatement(
                    "org.pkaq.core.mybatis.log.mapper.BusinessLogMapper." + method);
            Map<String, Object> parameters = new java.util.HashMap<>();
            parameters.put("id", 1L);
            parameters.put("tenantId", 12L);
            parameters.put("includeArchived", true);
            parameters.put("offset", 0L);
            parameters.put("limit", 10L);
            assertThrows(RuntimeException.class, () -> intercept(statement, statement.getBoundSql(parameters)));
            ThreadUser restricted = user("0007");
            ThreadUserHelper.runWithUser(restricted, () ->
                    assertThrows(RuntimeException.class, () -> intercept(statement, statement.getBoundSql(parameters))));
            ThreadUserHelper.runWithUser(user("0000"), () -> {
                BoundSql sql = statement.getBoundSql(parameters);
                intercept(statement, sql);
                assertTrue(sql.getSql().contains("LOG_BIZ_ARCHIVE"));
                assertFalse(sql.getSql().contains("CREATE_ID"));
                assertTrue(sql.getSql().contains("TENANT_ID"));
            });
        }
    }

    @Test
    void guardsErrorAndGeneratedCountEvenWhenGeneralDataPermissionIsDisabled() {
        config.getDataPermission().setEnable(false);
        for (String method : List.of("selectOne", "selectList", "selectPage", "selectPage_mpCount", "selectCount")) {
            Configuration configuration = new Configuration();
            MappedStatement statement = new MappedStatement.Builder(configuration,
                    "org.pkaq.core.mybatis.exception.mapper.ErrorIncidentMapper." + method,
                    new StaticSqlSource(configuration, "SELECT * FROM LOG_ERROR e WHERE e.TENANT_ID = 12"),
                    SqlCommandType.SELECT).build();
            assertThrows(RuntimeException.class, () -> intercept(statement, statement.getBoundSql(null)));
            ThreadUserHelper.runWithUser(user("unknown"), () ->
                    assertThrows(RuntimeException.class, () -> intercept(statement, statement.getBoundSql(null))));
            ThreadUserHelper.runWithUser(user("0000"), () -> intercept(statement, statement.getBoundSql(null)));
        }
    }

    private void intercept(MappedStatement statement, BoundSql sql) {
        try {
            interceptor.beforeQuery(null, statement, null, RowBounds.DEFAULT, null, sql);
        } catch (java.sql.SQLException exception) {
            throw new AssertionError(exception);
        }
    }

    private ThreadUser user(String scope) {
        return new ThreadUser().setUserId(11L).setTenantId(12L)
                .setDataScopes(List.of(new ThreadUser.DataScope(scope, List.of())));
    }
}
