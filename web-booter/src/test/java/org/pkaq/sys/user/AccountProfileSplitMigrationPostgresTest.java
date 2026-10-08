package org.pkaq.sys.user;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 在隔离 PostgreSQL 中验证账号资料拆分、引用保留和迁移边界。
 *
 * @author Codex
 * @date 2026-10-08
 */
class AccountProfileSplitMigrationPostgresTest {
    private static final String HASH_VALUE = "$2a$10$existing-password-hash-unchanged";

    /** 平台和登记租户保持账号 ID、凭据与关系，重复迁移不改写数据。 */
    @Test
    void shouldPreserveReferencesAcrossRegisteredSchemas() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate database = new JdbcTemplate(postgres.getPostgresDatabase());
            for (String schema : List.of("public", "tenant_101", "tenant_102", "unrelated_app")) {
                createLegacy(database, schema);
            }
            database.execute("CREATE TABLE SYS_TENANT(SCHEMA_NAME TEXT, DELETED BIGINT)");
            database.update("INSERT INTO SYS_TENANT VALUES ('tenant_101',0),('tenant_102',102),('missing_schema',0)");
            migrate(database, "db/migration/V12__ACCOUNT_PROFILE_SPLIT.sql");
            migrate(database, "db/migration/V12__ACCOUNT_PROFILE_SPLIT.sql");

            for (String schema : List.of("public", "tenant_101", "tenant_102")) {
                assertNull(database.queryForObject("SELECT to_regclass(?)::text", String.class, schema + ".sys_user"));
                assertEquals(HASH_VALUE, database.queryForObject(
                        "SELECT PASSWORD FROM " + schema + ".SYS_ACCOUNT WHERE ID=11", String.class));
                assertEquals(9L, database.queryForObject(
                        "SELECT PERM_VER FROM " + schema + ".SYS_ACCOUNT WHERE ID=11", Long.class));
                assertEquals("旧姓名", database.queryForObject(
                        "SELECT NAME FROM " + schema + ".SYS_ACCOUNT_PROFILE WHERE ACCOUNT_ID=11", String.class));
                assertEquals(7L, database.queryForObject(
                        "SELECT DEPT_ID FROM " + schema + ".SYS_ACCOUNT_PROFILE WHERE ACCOUNT_ID=11", Long.class));
                for (String relation : List.of("SYS_ROLEUSER_REF", "SYS_POSTUSER_REF", "SYS_LOGIN_LOG")) {
                    assertEquals(11L, database.queryForObject(
                            "SELECT USER_ID FROM " + schema + "." + relation, Long.class));
                }
                assertEquals(0, columns(database, schema, "sys_account", List.of("code", "name", "dept_id")));
                assertEquals(0, columns(database, schema, "sys_account_profile", List.of("password", "account")));
                database.update("INSERT INTO " + schema + ".SYS_ACCOUNT(ID,ACCOUNT,PASSWORD) VALUES(22,'only-account',?)",
                        HASH_VALUE);
                assertEquals(0, database.queryForObject("SELECT COUNT(*) FROM " + schema
                        + ".SYS_ACCOUNT_PROFILE WHERE ACCOUNT_ID=22", Integer.class));
                assertThrows(Exception.class, () -> database.update("INSERT INTO " + schema
                        + ".SYS_ACCOUNT_PROFILE(ACCOUNT_ID) VALUES(999)"));
                assertThrows(Exception.class, () -> database.update("INSERT INTO " + schema
                        + ".SYS_ACCOUNT(ID,ACCOUNT) VALUES(23,'original')"));
            }
            assertEquals(1, database.queryForObject("SELECT COUNT(*) FROM unrelated_app.SYS_USER", Integer.class));
            assertNull(database.queryForObject("SELECT to_regclass('unrelated_app.sys_account')::text", String.class));
            database.update("UPDATE tenant_101.SYS_ACCOUNT_PROFILE SET NAME='新姓名' WHERE ACCOUNT_ID=11");
            assertEquals("旧姓名", database.queryForObject(
                    "SELECT NAME FROM tenant_102.SYS_ACCOUNT_PROFILE WHERE ACCOUNT_ID=11", String.class));
        }
    }

    /** 租户独立升级脚本重复运行时保留既有账号资料。 */
    @Test
    void shouldSupportTenantOnlyMigrationAndRejectOverlap() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate database = new JdbcTemplate(postgres.getPostgresDatabase());
            createLegacy(database, "public");
            migrate(database, "db/tenant-migration/V2__ACCOUNT_PROFILE_SPLIT.sql");
            migrate(database, "db/tenant-migration/V2__ACCOUNT_PROFILE_SPLIT.sql");
            assertEquals(1, database.queryForObject("SELECT COUNT(*) FROM SYS_ACCOUNT_PROFILE", Integer.class));
            assertEquals(HASH_VALUE, database.queryForObject("SELECT PASSWORD FROM SYS_ACCOUNT", String.class));
            database.execute("CREATE TABLE SYS_USER(ID BIGINT)");
            assertThrows(Exception.class, () -> migrate(database, "db/tenant-migration/V2__ACCOUNT_PROFILE_SPLIT.sql"));
            assertEquals(1, database.queryForObject("SELECT COUNT(*) FROM SYS_ACCOUNT", Integer.class));
        }
    }

    /** 登记租户出现旧新表重叠时，整个升级必须回滚而不是部分完成。 */
    @Test
    void shouldRollbackAllSchemasWhenOneTenantIsInvalid() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate database = new JdbcTemplate(postgres.getPostgresDatabase());
            createLegacy(database, "public");
            createLegacy(database, "tenant_101");
            database.execute("CREATE TABLE SYS_TENANT(SCHEMA_NAME TEXT)");
            database.update("INSERT INTO SYS_TENANT VALUES('tenant_101')");
            database.execute("CREATE TABLE tenant_101.SYS_ACCOUNT(ID BIGINT)");
            assertThrows(Exception.class, () -> migrate(database, "db/migration/V12__ACCOUNT_PROFILE_SPLIT.sql"));
            assertEquals(1, database.queryForObject("SELECT COUNT(*) FROM public.SYS_USER", Integer.class));
            assertNull(database.queryForObject("SELECT to_regclass('public.sys_account')::text", String.class));
            assertNull(database.queryForObject("SELECT to_regclass('public.sys_account_profile')::text", String.class));
        }
    }

    /** 全量初始化脚本必须直接创建新账号结构并绑定管理员资料。 */
    @Test
    void shouldBootstrapNewAccountTables() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate database = new JdbcTemplate(postgres.getPostgresDatabase());
            Path script = Path.of("docs/sql/system_management_full_postgresql.sql");
            if (!Files.exists(script)) {
                script = Path.of("../docs/sql/system_management_full_postgresql.sql");
            }
            database.execute(Files.readString(script));
            assertNull(database.queryForObject("SELECT to_regclass('sys_user')::text", String.class));
            assertEquals(1, database.queryForObject("SELECT COUNT(*) FROM SYS_ACCOUNT a "
                    + "JOIN SYS_ACCOUNT_PROFILE p ON p.ACCOUNT_ID=a.ID WHERE a.ACCOUNT='admin'", Integer.class));
            assertEquals(0, columns(database, "public", "sys_account", List.of("code", "name", "dept_id")));
            assertEquals(0, columns(database, "public", "sys_account_profile", List.of("password", "account")));
        }
    }

    private void migrate(JdbcTemplate database, String resource) throws Exception {
        database.execute(new ClassPathResource(resource).getContentAsString(StandardCharsets.UTF_8));
    }

    private int columns(JdbcTemplate database, String schema, String table, List<String> names) {
        return database.queryForObject("SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_schema=? AND table_name=? AND column_name IN (?,?,?)", Integer.class,
                schema, table, names.get(0), names.get(1), names.size() > 2 ? names.get(2) : names.get(1));
    }

    private void createLegacy(JdbcTemplate database, String schema) {
        database.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        database.execute("CREATE TABLE " + schema + ".SYS_USER(ID BIGINT PRIMARY KEY, ACCOUNT TEXT UNIQUE, "
                + "PASSWORD TEXT, NICK_NAME TEXT, EMAIL TEXT, TEL TEXT, PERM_VER BIGINT, DELETED BIGINT, "
                + "FROZEN INTEGER, CODE VARCHAR(100), NAME VARCHAR(100), DEPT_ID BIGINT)");
        database.update("INSERT INTO " + schema + ".SYS_USER VALUES(11,'original',?,'昵称','test@example.test',"
                + "'00000000000',9,0,0,'EMP11','旧姓名',7)", HASH_VALUE);
        for (String relation : List.of("SYS_ROLEUSER_REF", "SYS_POSTUSER_REF", "SYS_LOGIN_LOG")) {
            database.execute("CREATE TABLE " + schema + "." + relation
                    + "(USER_ID BIGINT REFERENCES " + schema + ".SYS_USER(ID))");
            database.update("INSERT INTO " + schema + "." + relation + " VALUES(11)");
        }
    }
}
