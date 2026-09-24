package org.pkaq.sys.tenant.service;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.mybatis.tenant.TargetTenantExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchemaRouter;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 平台跨租户查看的 PostgreSQL schema 隔离测试。
 *
 * @author PKAQ
 */
class PlatformTenantInspectSchemaPostgresTest {
    @Test
    void readsTenantAAndBWithoutLeakAndRestoresAfterFailure() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            seed(jdbcTemplate);
            EvaConfig config = schemaConfig();
            TenantSchemaRouter router = new TenantSchemaRouter(dataSource, config,
                    tenantId -> "tenant_" + tenantId);
            TargetTenantExecutor executor = new TargetTenantExecutor(config, router);
            TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));

            ThreadUserHelper.runWithUser(platformAdministrator(), () -> transaction.executeWithoutResult(status -> {
                String original = jdbcTemplate.queryForObject(
                        "SELECT current_setting('search_path')", String.class);
                assertEquals("Org A", executor.execute(101L, () -> jdbcTemplate.queryForObject(
                        "SELECT NAME FROM SYS_ORGANIZATION", String.class)));
                assertEquals(original, jdbcTemplate.queryForObject(
                        "SELECT current_setting('search_path')", String.class));
                assertEquals("Role B", executor.execute(102L, () -> jdbcTemplate.queryForObject(
                        "SELECT NAME FROM SYS_ROLE", String.class)));
                assertEquals(original, jdbcTemplate.queryForObject(
                        "SELECT current_setting('search_path')", String.class));

                assertThrows(IllegalStateException.class, () -> executor.execute(101L, () -> {
                    throw new IllegalStateException("expected");
                }));
                assertEquals(original, jdbcTemplate.queryForObject(
                        "SELECT current_setting('search_path')", String.class));
            }));

            ThreadUserHelper.runWithUser(tenantAdministrator(), () ->
                    assertThrows(SecurityException.class, () -> transaction.execute(status ->
                            executor.execute(102L, () -> "denied"))));
        }
    }

    private void seed(JdbcTemplate jdbcTemplate) {
        for (long tenantId : new long[]{101L, 102L}) {
            String schema = "tenant_" + tenantId;
            jdbcTemplate.execute("CREATE SCHEMA " + schema);
            jdbcTemplate.execute("CREATE TABLE " + schema
                    + ".SYS_ORGANIZATION(ID BIGINT PRIMARY KEY, NAME VARCHAR(100))");
            jdbcTemplate.execute("CREATE TABLE " + schema
                    + ".SYS_ROLE(ID BIGINT PRIMARY KEY, NAME VARCHAR(100))");
        }
        jdbcTemplate.update("INSERT INTO tenant_101.SYS_ORGANIZATION VALUES (1, 'Org A')");
        jdbcTemplate.update("INSERT INTO tenant_102.SYS_ORGANIZATION VALUES (1, 'Org B')");
        jdbcTemplate.update("INSERT INTO tenant_101.SYS_ROLE VALUES (1, 'Role A')");
        jdbcTemplate.update("INSERT INTO tenant_102.SYS_ROLE VALUES (1, 'Role B')");
    }

    private EvaConfig schemaConfig() {
        EvaConfig config = new EvaConfig();
        config.setMode(CommonConstant.MODE_PLATFORM);
        TenantProperties tenant = new TenantProperties();
        tenant.setEnable(true);
        tenant.setMode(TenantProperties.MODE_SCHEMA);
        tenant.setCoreSchema("public");
        tenant.setPrefix("tenant_");
        config.setTenant(tenant);
        return config;
    }

    private ThreadUser platformAdministrator() {
        return new ThreadUser().setTenantId(0L).setRolesMap(Map.of(1L,
                new ThreadUser.GrantedRoles("平台管理员", CommonConstant.ADMIN_ROLE_NAME)));
    }

    private ThreadUser tenantAdministrator() {
        return new ThreadUser().setTenantId(101L).setRolesMap(Map.of(1L,
                new ThreadUser.GrantedRoles("租户管理员", CommonConstant.ADMIN_ROLE_NAME)));
    }
}
