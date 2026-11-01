package org.pkaq.sys.tenant;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.pkaq.core.mybatis.tenant.TrustedTenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.sys.tenant.service.TenantSchemaProvisioner;
import org.pkaq.test.PostgresInstanceEvidence;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用自有合成PostgreSQL验证单初始化最终状态，不连接应用或开发配置。
 * @author PKAQ
 * @date 2026-10-31
 */
class SingleInitializationPostgresTest {
    private static final long ADMIN = 1000000000000000005L;

    /** 正式Flyway只发现执行一个版本，覆盖最终结构、种子、约束和再次运行。 */
    @Test
    void initializesOneVersionWithFinalObjectsAndApprovedSeeds() throws Exception {
        try (EmbeddedPostgres postgres = fixture()) {
            DataSource source = postgres.getPostgresDatabase();
            PostgresInstanceEvidence.capture(source, "init-empty");
            Flyway flyway = configured(source);
            assertEquals(1, flyway.info().pending().length);
            assertEquals(1, flyway.migrate().migrationsExecuted);
            JdbcTemplate database = new JdbcTemplate(source);
            assertEquals(List.of("1"), database.queryForList(
                    "SELECT version FROM eva.flyway_schema_history WHERE success AND version IS NOT NULL",
                    String.class));
            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertTrue(flyway.validateWithResult().validationSuccessful);
            for (String obsolete : List.of("sys_user", "black_list")) {
                assertNull(database.queryForObject("SELECT to_regclass(?)::text", String.class, "eva." + obsolete));
            }
            assertEquals("admin", database.queryForObject(
                    "SELECT account FROM eva.sys_account WHERE id=?", String.class, ADMIN));
            assertEquals(1, database.queryForObject(
                    "SELECT count(*) FROM eva.sys_account_profile WHERE account_id=?", Integer.class, ADMIN));
            assertEquals(1, database.queryForObject(
                    "SELECT count(*) FROM eva.sys_roleuser_ref WHERE user_id=?", Integer.class, ADMIN));
            assertEquals(1, database.queryForObject(
                    "SELECT count(*) FROM eva.sys_postuser_ref WHERE user_id=?", Integer.class, ADMIN));
            assertEquals(0, columns(database, "eva", "sys_account", List.of("code", "name", "dept_id")));
            assertEquals(0, columns(database, "eva", "log_error", List.of("ex_desc")));
            assertEquals(2, columns(database, "eva", "log_biz", List.of("user_id", "success")));
            assertEquals(3, database.queryForObject("SELECT count(*) FROM information_schema.columns "
                    + "WHERE table_schema='eva' AND table_name IN ('log_biz','log_error','sys_login_log') "
                    + "AND column_name='tenant_id' AND is_nullable='NO' AND column_default IS NOT NULL",
                    Integer.class));
            assertEquals(12, columns(database, "eva", "sys_login_log", List.of("session_id", "device_type",
                    "device_model", "os_name", "os_version", "browser_name", "browser_version",
                    "device_fingerprint", "risk_flags", "last_active_at", "logout_at", "logout_reason")));
            for (String table : List.of("log_biz_archive", "sys_notice", "sys_tenant_package_grant")) {
                assertEquals("eva." + table, database.queryForObject(
                        "SELECT to_regclass(?)::text", String.class, "eva." + table));
            }
            assertEquals(8, database.queryForObject("SELECT count(*) FROM eva.sys_dict WHERE category='SYSTEM'",
                    Integer.class));
            assertEquals(0, database.queryForObject("SELECT count(*) FROM eva.sys_dict WHERE category IS NULL "
                    + "OR code LIKE 'EVA_QA_%' OR code IN ('SYSTEM_DICT','SYSTEM','BUSINESS')", Integer.class));
            assertEquals(0, database.queryForObject("SELECT count(*) FROM eva.sys_dict_item i "
                    + "LEFT JOIN eva.sys_dict d ON d.id=i.main_id WHERE d.id IS NULL", Integer.class));
            assertEquals(List.of("0000", "0001", "0002", "0003", "0006", "0007"), database.queryForList(
                    "SELECT d_code FROM eva.sys_dict_item WHERE main_id=1000000000000004001 ORDER BY d_code",
                    String.class));
            assertEquals("/log/login", database.queryForObject(
                    "SELECT route_url FROM eva.sys_module WHERE id=1000000000000000204", String.class));
            assertEquals(1000000000000000204L, database.queryForObject(
                    "SELECT main_id FROM eva.sys_module_resources WHERE id=1000000000000001407", Long.class));
            assertEquals(16, database.queryForObject("SELECT count(*) FROM eva.sys_module_resources "
                    + "WHERE id BETWEEN 1000000000000001401 AND 1000000000000001416", Integer.class));
            assertEquals(3, database.queryForObject("SELECT count(*) FROM eva.sys_module_resources "
                    + "WHERE id BETWEEN 1000000000000001414 AND 1000000000000001416 "
                    + "AND resource_type='GET'", Integer.class));
            assertEquals(0, database.queryForObject("SELECT count(*) FROM eva.sys_roleres_ref "
                    + "WHERE role_id<>1000000000000000004", Integer.class));
            assertEquals(16, database.queryForObject("SELECT count(*) FROM eva.sys_roleres_ref "
                    + "WHERE role_id=1000000000000000004 "
                    + "AND resource_id BETWEEN 1000000000000001401 AND 1000000000000001416", Integer.class));
            assertEquals(0, database.queryForObject("SELECT count(*) FROM eva.sys_tenant_resource", Integer.class));
            assertIndexes(database);
            assertThrows(Exception.class, () -> database.update(
                    "INSERT INTO eva.sys_dict(id,code,category) VALUES(1,'bad','UNKNOWN')"));
            assertThrows(Exception.class, () -> database.update(
                    "INSERT INTO eva.sys_account_profile(account_id) VALUES(1)"));
            database.update("INSERT INTO eva.sys_tenant(id,code,schema_name) VALUES(0,'eva',NULL)");
            database.update("INSERT INTO eva.sys_tenant(id,code,schema_name,deleted) "
                    + "VALUES(1,'acme','tenant_acme',1)");
            assertThrows(Exception.class, () -> database.update(
                    "INSERT INTO eva.sys_tenant(id,code) VALUES(2,'acme')"));
            assertThrows(Exception.class, () -> database.update(
                    "INSERT INTO eva.sys_tenant(id,code) VALUES(2,'TOOLONG')"));
            assertThrows(Exception.class, () -> database.update(
                    "INSERT INTO eva.sys_tenant(id,code,schema_name) VALUES(2,'beta','tenant_acme')"));
            database.execute("SELECT eva.eva_provision_tenant_schema('tenant_empty')");
            assertThrows(Exception.class, () -> database.execute(
                    "SELECT eva.eva_provision_tenant_schema('tenant_empty')"));
        }
    }

    /** 已有升级链的真实history必须校验失败，不自动修复或重跑初始化。 */
    @Test
    void rejectsExistingDifferentHistoryWithoutChangingItsObjects() throws Exception {
        try (EmbeddedPostgres postgres = fixture()) {
            DataSource source = postgres.getPostgresDatabase();
            PostgresInstanceEvidence.capture(source, "init-history");
            Path old = Files.createTempDirectory("old-unreleased-migration-");
            try {
                Files.writeString(old.resolve("V1__INITDB.sql"), "CREATE TABLE legacy_marker(id bigint);");
                Files.writeString(old.resolve("V2__OLD.sql"), "INSERT INTO legacy_marker VALUES(1);");
                Flyway previous = Flyway.configure().dataSource(source).schemas("eva").defaultSchema("eva")
                        .locations("filesystem:" + old).load();
                assertEquals(2, previous.migrate().migrationsExecuted);
                Flyway current = configured(source);
                assertFalse(current.validateWithResult().validationSuccessful);
                assertThrows(Exception.class, current::migrate);
                JdbcTemplate database = new JdbcTemplate(source);
                assertEquals(1, database.queryForObject("SELECT count(*) FROM eva.legacy_marker", Integer.class));
                assertEquals(2, database.queryForObject(
                        "SELECT count(*) FROM eva.flyway_schema_history WHERE success AND version IS NOT NULL",
                        Integer.class));
                assertNull(database.queryForObject("SELECT to_regclass('eva.sys_account')::text", String.class));
                database.execute("CREATE SCHEMA legacy_untracked");
                database.execute("CREATE TABLE legacy_untracked.marker(id bigint)");
                Flyway untracked = Flyway.configure().dataSource(source).schemas("legacy_untracked")
                        .defaultSchema("legacy_untracked").locations("classpath:db/migration")
                        .baselineOnMigrate(false).cleanDisabled(true).load();
                assertThrows(Exception.class, untracked::migrate);
                assertNull(database.queryForObject(
                        "SELECT to_regclass('legacy_untracked.sys_account')::text", String.class));
                assertNull(database.queryForObject(
                        "SELECT to_regclass('legacy_untracked.flyway_schema_history')::text", String.class));
            } finally {
                Files.deleteIfExists(old.resolve("V1__INITDB.sql"));
                Files.deleteIfExists(old.resolve("V2__OLD.sql"));
                Files.deleteIfExists(old);
            }
        }
    }

    /** 实际provisioner只执行最终模板，保持租户隔离、账本幂等与旧链拒绝。 */
    @Test
    void provisionsFinalTenantTemplateAndRefusesLegacyLedger() throws Exception {
        try (EmbeddedPostgres postgres = fixture()) {
            DataSource source = postgres.getPostgresDatabase();
            PostgresInstanceEvidence.capture(source, "init-tenant");
            configured(source).migrate();
            JdbcTemplate database = new JdbcTemplate(source);
            database.update("INSERT INTO eva.sys_tenant(id,code) VALUES(101,'acme'),(102,'beta')");
            EvaConfig config = new EvaConfig();
            config.getTenant().setEnable(true);
            config.getTenant().setMode(TenantProperties.MODE_SCHEMA);
            config.getTenant().setCoreSchema("eva");
            config.getTenant().setPrefix("tenant_");
            TenantSchemaProvisioner provisioner = new TenantSchemaProvisioner(database, source, config,
                    new TrustedTenantSchemaResolver(database, config));
            TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
            transaction.executeWithoutResult(status -> {
                provisioner.provision(101L);
                provisioner.provision(102L);
            });
            database.update("INSERT INTO tenant_acme.sys_account(id,account) VALUES(11,'tenant-fixture')");
            database.update("INSERT INTO tenant_acme.sys_account_profile(account_id,name) VALUES(11,'A')");
            transaction.executeWithoutResult(status -> provisioner.provision(101L));
            for (String schema : List.of("tenant_acme", "tenant_beta")) {
                assertEquals(List.of("1"), database.queryForList(
                        "SELECT version FROM " + schema + ".eva_tenant_schema_version", String.class));
                assertEquals(0, columns(database, schema, "sys_account", List.of("tenant_id", "code", "dept_id")));
                assertEquals(1, columns(database, schema, "sys_dict", List.of("category")));
                assertEquals(0, database.queryForObject("SELECT count(*) FROM " + schema + ".sys_dict", Integer.class));
                for (String absent : List.of("sys_user", "sys_module", "log_biz", "sys_login_log", "sys_tenant")) {
                    assertNull(database.queryForObject(
                            "SELECT to_regclass(?)::text", String.class, schema + "." + absent));
                }
                assertEquals(1, database.queryForObject("SELECT count(*) FROM information_schema.tables "
                        + "WHERE table_schema=? AND table_name='sys_notice'", Integer.class, schema));
            }
            assertEquals(0, database.queryForObject("SELECT count(*) FROM tenant_beta.sys_account", Integer.class));
            assertEquals("A", database.queryForObject(
                    "SELECT name FROM tenant_acme.sys_account_profile WHERE account_id=11", String.class));
            database.update("INSERT INTO tenant_acme.eva_tenant_schema_version(version) VALUES('2')");
            assertThrows(IllegalStateException.class, () ->
                    transaction.executeWithoutResult(status -> provisioner.provision(101L)));
            assertEquals(2, database.queryForObject(
                    "SELECT count(*) FROM tenant_acme.eva_tenant_schema_version", Integer.class));
            assertEquals("A", database.queryForObject(
                    "SELECT name FROM tenant_acme.sys_account_profile WHERE account_id=11", String.class));
            database.execute("CREATE SCHEMA tenant_old");
            database.execute("CREATE TABLE tenant_old.marker(id bigint)");
            database.update("INSERT INTO tenant_old.marker VALUES(1)");
            database.execute("CREATE SCHEMA tenant_older");
            database.execute("CREATE TABLE tenant_older.sys_role(id bigint)");
            database.update("INSERT INTO eva.sys_tenant(id,code,schema_name) "
                    + "VALUES(103,'old','tenant_old'),(104,'older','tenant_older')");
            for (long id : List.of(103L, 104L)) {
                assertThrows(IllegalStateException.class, () ->
                        transaction.executeWithoutResult(status -> provisioner.provision(id)));
            }
            for (String schema : List.of("tenant_old", "tenant_older")) {
                assertNull(database.queryForObject(
                        "SELECT to_regclass(?)::text", String.class, schema + ".sys_account"));
                assertNull(database.queryForObject(
                        "SELECT to_regclass(?)::text", String.class, schema + ".eva_tenant_schema_version"));
            }
            assertEquals(1, database.queryForObject("SELECT count(*) FROM tenant_old.marker", Integer.class));
            assertEquals("tenant_older.sys_role", database.queryForObject(
                    "SELECT to_regclass('tenant_older.sys_role')::text", String.class));
            assertEquals("public", database.queryForObject("SELECT current_schema()", String.class));
        }
    }

    private EmbeddedPostgres fixture() throws Exception {
        return EmbeddedPostgres.builder().setServerConfig("listen_addresses", "127.0.0.1").setPort(0).start();
    }

    private Flyway configured(DataSource source) {
        return Flyway.configure().dataSource(source).schemas("eva").defaultSchema("eva")
                .locations("classpath:db/migration").baselineOnMigrate(false).cleanDisabled(true).load();
    }

    private int columns(JdbcTemplate database, String schema, String table, List<String> names) {
        return database.queryForList("SELECT column_name FROM information_schema.columns "
                        + "WHERE table_schema=? AND table_name=?", String.class, schema, table)
                .stream().filter(names::contains).toList().size();
    }

    private void assertIndexes(JdbcTemplate database) {
        Set<String> indexes = Set.copyOf(database.queryForList(
                "SELECT indexname FROM pg_indexes WHERE schemaname='eva'", String.class));
        assertTrue(indexes.containsAll(Set.of("uk_sys_tenant_code", "uk_sys_tenant_schema_name",
                "uk_sys_role_tenant_system", "idx_sys_tenant_role_active", "idx_sys_role_template",
                "idx_sys_tenant_package_grant_active", "uk_sys_dict_code", "uk_sys_dict_item_main_code",
                "uk_log_error_tenant_fingerprint", "idx_log_error_tenant_last_occurred",
                "idx_log_biz_tenant_time_id", "idx_log_biz_tenant_module_type_time",
                "idx_log_biz_tenant_user_time", "idx_log_biz_tenant_biz_time",
                "idx_log_biz_archive_tenant_time_id", "idx_log_biz_archive_tenant_module_type_time",
                "idx_log_biz_archive_tenant_user_time", "idx_log_biz_archive_tenant_biz_time",
                "idx_sys_login_log_user_device", "idx_sys_account_profile_dept_id",
                "idx_sys_account_profile_code")));
    }
}
