package org.pkaq.sys.tenant;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 既有租户通知表与接口资源权限迁移验证。
 *
 * @author PKAQ
 */
class TenantUpgradeMigrationPostgresTest {

    @Test
    void upgradesExistingTenantSchemaIdempotently() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(postgres.getPostgresDatabase());
            jdbcTemplate.execute("CREATE TABLE SYS_TENANT (ID BIGINT PRIMARY KEY, DELETED BIGINT, SCHEMA_NAME VARCHAR(63))");
            jdbcTemplate.execute("CREATE SCHEMA tenant_42");
            jdbcTemplate.update("INSERT INTO SYS_TENANT(ID, DELETED, SCHEMA_NAME) VALUES (42, 0, 'tenant_42')");

            executeMigration(jdbcTemplate, "db/migration/V6__UPGRADE_EXISTING_TENANT_NOTICE.sql");
            executeMigration(jdbcTemplate, "db/migration/V6__UPGRADE_EXISTING_TENANT_NOTICE.sql");

            assertEquals(1, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = 'tenant_42' AND table_name = 'sys_notice'
                    """, Integer.class));
            assertEquals(3, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM pg_indexes
                    WHERE schemaname = 'tenant_42' AND tablename = 'sys_notice'
                    """, Integer.class));
        }
    }

    @Test
    void addsApiResourcesWithoutExpandingModuleBoundary() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(postgres.getPostgresDatabase());
            createPermissionTables(jdbcTemplate);
            jdbcTemplate.update("""
                    INSERT INTO SYS_MODULE_RESOURCES(ID, MAIN_ID, CODE, RESOURCE_URL, RESOURCE_TYPE, DELETED)
                    VALUES (1, 1000000000000000201, 'MENU', '/log/online', '*', 0)
                    """);
            jdbcTemplate.update("INSERT INTO SYS_ROLERES_REF(ROLE_ID, RESOURCE_ID) VALUES (99, 1)");
            jdbcTemplate.update("INSERT INTO SYS_TENANT_RESOURCE(TENANT_ID, RESOURCE_ID) VALUES (42, 1)");

            executeMigration(jdbcTemplate, "db/migration/V7__API_RESOURCE_PERMISSION.sql");
            executeMigration(jdbcTemplate, "db/migration/V7__API_RESOURCE_PERMISSION.sql");

            assertEquals(13, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM SYS_MODULE_RESOURCES
                    WHERE ID BETWEEN 1000000000000001401 AND 1000000000000001413
                    """, Integer.class));
            assertEquals(13, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM SYS_ROLERES_REF
                    WHERE ROLE_ID = 1000000000000000004
                      AND RESOURCE_ID BETWEEN 1000000000000001401 AND 1000000000000001413
                    """, Integer.class));
            assertEquals(1, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM SYS_ROLERES_REF
                    WHERE ROLE_ID = 99 AND RESOURCE_ID = 1000000000000001404
                    """, Integer.class));
            assertEquals(0, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM SYS_ROLERES_REF
                    WHERE ROLE_ID = 99 AND RESOURCE_ID = 1000000000000001405
                    """, Integer.class));
            assertEquals(1, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM SYS_TENANT_RESOURCE
                    WHERE TENANT_ID = 42 AND RESOURCE_ID = 1000000000000001404
                    """, Integer.class));
        }
    }

    private void createPermissionTables(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE SYS_MODULE_RESOURCES (
                    ID BIGINT PRIMARY KEY, MAIN_ID BIGINT, TENANT_ID BIGINT, CREATE_ID BIGINT,
                    CREATE_BY VARCHAR(100), UTC_CREATE TIMESTAMP, MODIFY_ID BIGINT,
                    MODIFY_BY VARCHAR(100), UTC_MODIFY TIMESTAMP, CODE VARCHAR(100),
                    RESOURCE_DESC VARCHAR(200), RESOURCE_URL VARCHAR(300), RESOURCE_TYPE VARCHAR(16),
                    SORT DOUBLE PRECISION, DELETED BIGINT
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE SYS_ROLERES_REF (
                    ROLE_ID BIGINT NOT NULL, RESOURCE_ID BIGINT NOT NULL,
                    PRIMARY KEY (ROLE_ID, RESOURCE_ID)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE SYS_TENANT_RESOURCE (
                    TENANT_ID BIGINT NOT NULL, RESOURCE_ID BIGINT NOT NULL,
                    PRIMARY KEY (TENANT_ID, RESOURCE_ID)
                )
                """);
    }

    private void executeMigration(JdbcTemplate jdbcTemplate, String path) throws Exception {
        ClassPathResource migration = new ClassPathResource(path);
        String script = StreamUtils.copyToString(migration.getInputStream(), StandardCharsets.UTF_8);
        jdbcTemplate.execute(script);
    }
}
