package org.pkaq.sys.tenant;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用真实 Flyway PostgreSQL 解析器验证完整迁移链，仅使用隔离临时数据库。
 *
 * @author Codex
 * @date 2026-10-09
 */
class TenantCodeFlywayChainPostgresTest {
    private static final long ADMIN_ID = 1000000000000000005L;

    /** 全新数据库必须完整初始化至 V13，重复执行不得重新运行已完成迁移。 */
    @Test
    void initializesEmptyDatabaseThroughAllThirteenMigrations() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource source = postgres.getPostgresDatabase();
            Flyway flyway = configured(source, null);
            assertEquals(13, flyway.migrate().migrationsExecuted);
            JdbcTemplate database = new JdbcTemplate(source);

            assertEquals(13, database.queryForObject(
                    "SELECT COUNT(*) FROM eva.flyway_schema_history WHERE success AND version IS NOT NULL",
                    Integer.class));
            assertEquals("admin", database.queryForObject(
                    "SELECT account FROM eva.sys_account WHERE id = ?", String.class, ADMIN_ID));
            assertEquals(1, database.queryForObject(
                    "SELECT COUNT(*) FROM eva.sys_account_profile WHERE account_id = ?", Integer.class, ADMIN_ID));
            assertEquals(1, database.queryForObject(
                    "SELECT COUNT(*) FROM eva.sys_roleuser_ref WHERE user_id = ?", Integer.class, ADMIN_ID));
            assertEquals(1, database.queryForObject(
                    "SELECT COUNT(*) FROM eva.sys_postuser_ref WHERE user_id = ?", Integer.class, ADMIN_ID));
            assertNull(database.queryForObject("SELECT to_regclass('eva.sys_user')::text", String.class));
            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertTrue(flyway.validateWithResult().validationSuccessful);
            assertThrows(Exception.class, () -> database.update(
                    "INSERT INTO eva.sys_tenant(id, code) VALUES (8, NULL)"));
            assertThrows(Exception.class, () -> database.update(
                    "INSERT INTO eva.sys_tenant(id, code) VALUES (8, 'TOOLONG')"));
        }
    }

    /** V12 升级旧租户账号后，V13 重命名 schema 必须保留主键、凭据和平台零号记录。 */
    @Test
    void upgradesLegacyTenantAccountBeforeRenamingSchema() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource source = postgres.getPostgresDatabase();
            assertEquals(11, configured(source, "11").migrate().migrationsExecuted);
            JdbcTemplate database = new JdbcTemplate(source);
            database.execute("CREATE SCHEMA tenant_1");
            database.execute("CREATE TABLE tenant_1.sys_user (LIKE eva.sys_user INCLUDING ALL)");
            database.update("INSERT INTO tenant_1.sys_user SELECT * FROM eva.sys_user WHERE id = ?", ADMIN_ID);
            database.update("INSERT INTO eva.sys_tenant(id, code, schema_name) VALUES (0, 'eva', NULL)");
            database.update("INSERT INTO eva.sys_tenant(id, code, schema_name) VALUES (1, 'acme1', 'tenant_1')");
            String passwordHash = database.queryForObject(
                    "SELECT password FROM tenant_1.sys_user WHERE id = ?", String.class, ADMIN_ID);

            Flyway flyway = configured(source, null);
            assertEquals(2, flyway.migrate().migrationsExecuted);
            assertFalse(Boolean.TRUE.equals(database.queryForObject(
                    "SELECT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'tenant_1')", Boolean.class)));
            assertEquals("tenant_acme1", database.queryForObject(
                    "SELECT schema_name FROM eva.sys_tenant WHERE id = 1", String.class));
            assertEquals(passwordHash, database.queryForObject(
                    "SELECT password FROM tenant_acme1.sys_account WHERE id = ?", String.class, ADMIN_ID));
            assertEquals(1, database.queryForObject(
                    "SELECT COUNT(*) FROM tenant_acme1.sys_account_profile WHERE account_id = ?",
                    Integer.class, ADMIN_ID));
            assertEquals("eva", database.queryForObject(
                    "SELECT code FROM eva.sys_tenant WHERE id = 0", String.class));
            assertNull(database.queryForObject(
                    "SELECT schema_name FROM eva.sys_tenant WHERE id = 0", String.class));
            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertTrue(flyway.validateWithResult().validationSuccessful);
        }
    }

    /** 后续租户冲突时，真实 Flyway 必须回滚先前重命名及映射写入。 */
    @Test
    void rollsBackEarlierRenameWhenLaterTenantTargetIsOccupied() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource source = postgres.getPostgresDatabase();
            assertEquals(12, configured(source, "12").migrate().migrationsExecuted);
            JdbcTemplate database = new JdbcTemplate(source);
            database.execute("CREATE SCHEMA tenant_1");
            database.execute("CREATE SCHEMA tenant_2");
            database.execute("CREATE SCHEMA tenant_beta");
            database.execute("CREATE TABLE tenant_1.sentinel(value VARCHAR(16))");
            database.update("INSERT INTO tenant_1.sentinel(value) VALUES ('preserved')");
            database.update("""
                    INSERT INTO eva.sys_tenant(id, code, schema_name)
                    VALUES (1, 'acme', 'tenant_1'), (2, 'beta', 'tenant_2')
                    """);

            assertThrows(Exception.class, () -> configured(source, null).migrate());
            assertEquals("preserved", database.queryForObject(
                    "SELECT value FROM tenant_1.sentinel", String.class));
            assertNull(database.queryForObject("SELECT to_regnamespace('tenant_acme')::text", String.class));
            assertEquals("tenant_1", database.queryForObject(
                    "SELECT schema_name FROM eva.sys_tenant WHERE id = 1", String.class));
            assertEquals("tenant_2", database.queryForObject(
                    "SELECT schema_name FROM eva.sys_tenant WHERE id = 2", String.class));
            assertEquals(0, database.queryForObject(
                    "SELECT COUNT(*) FROM eva.flyway_schema_history WHERE version = '13'", Integer.class));
            assertEquals(12, database.queryForObject(
                    "SELECT COUNT(*) FROM eva.flyway_schema_history WHERE success AND version IS NOT NULL",
                    Integer.class));
        }
    }

    /** 构造测试专用迁移器，固定临时数据库内的平台 schema，禁止读取开发配置。 */
    private Flyway configured(DataSource source, String target) {
        var configuration = Flyway.configure().dataSource(source)
                .locations("classpath:db/migration").schemas("eva").defaultSchema("eva");
        if (null != target) {
            configuration.target(target);
        }
        return configuration.load();
    }
}
