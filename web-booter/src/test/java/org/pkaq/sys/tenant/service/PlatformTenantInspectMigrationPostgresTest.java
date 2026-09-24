package org.pkaq.sys.tenant.service;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 平台跨租户只读资源迁移测试。
 *
 * @author PKAQ
 */
class PlatformTenantInspectMigrationPostgresTest {
    @Test
    void registersExactGetResourcesForPlatformAdministratorOnly() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute("""
                    CREATE TABLE SYS_MODULE_RESOURCES(
                        ID BIGINT PRIMARY KEY, MAIN_ID BIGINT, TENANT_ID BIGINT,
                        CREATE_ID BIGINT, CREATE_BY VARCHAR(100), UTC_CREATE TIMESTAMP,
                        MODIFY_ID BIGINT, MODIFY_BY VARCHAR(100), UTC_MODIFY TIMESTAMP,
                        CODE VARCHAR(100), RESOURCE_DESC VARCHAR(200), RESOURCE_URL VARCHAR(300),
                        RESOURCE_TYPE VARCHAR(20), SORT DOUBLE PRECISION, DELETED BIGINT)
                    """);
            jdbcTemplate.execute("""
                    CREATE TABLE SYS_ROLERES_REF(
                        ROLE_ID BIGINT, RESOURCE_ID BIGINT,
                        PRIMARY KEY(ROLE_ID, RESOURCE_ID))
                    """);

            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection,
                        new ClassPathResource("db/migration/V10__PLATFORM_TENANT_INSPECT.sql"));
            }

            assertEquals(3, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM SYS_MODULE_RESOURCES
                    WHERE ID BETWEEN 1000000000000001414 AND 1000000000000001416
                      AND RESOURCE_TYPE = 'GET'
                    """, Integer.class));
            assertEquals(3, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM SYS_ROLERES_REF
                    WHERE ROLE_ID = 1000000000000000004
                      AND RESOURCE_ID BETWEEN 1000000000000001414 AND 1000000000000001416
                    """, Integer.class));
            assertEquals(0, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM SYS_ROLERES_REF
                    WHERE ROLE_ID <> 1000000000000000004
                      AND RESOURCE_ID BETWEEN 1000000000000001414 AND 1000000000000001416
                    """, Integer.class));
        }
    }
}
