package org.pkaq.sys.tenant.service;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.pkaq.core.mybatis.tenant.TargetTenantExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchemaRouter;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.BCryptUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证平台租户维护不会写入 core 或相邻租户 schema。
 *
 * @author PKAQ
 */
class TenantPrivateAccountSchemaPostgresTest {

    @Test
    void createsReadOnlyRootOrganizationAndBindsAdministrator() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            createSchema(jdbcTemplate, "public");
            createSchema(jdbcTemplate, "tenant_101");

            EvaConfig config = schemaConfig();
            TenantSchemaRouter router = new TenantSchemaRouter(dataSource, config,
                    tenantId -> "tenant_" + tenantId);
            TenantPrivateAccountService service = new TenantPrivateAccountService(
                    jdbcTemplate, new TargetTenantExecutor(config, router));
            TransactionTemplate transaction = new TransactionTemplate(
                    new DataSourceTransactionManager(dataSource));

            ThreadUserHelper.runWithUser(platformAdministrator(), () -> transaction.executeWithoutResult(status ->
                    service.createRootOrganizationAndAdministrator(101L, 1011L, "T101", "测试租户",
                            "tenant-admin", "ChangeMe123!")));

            Map<String, Object> organization = jdbcTemplate.queryForMap("""
                    SELECT ID, NAME, CODE, PID, PATH, ISLEAF, FROZEN
                    FROM tenant_101.SYS_ORGANIZATION
                    """);
            long organizationId = ((Number) organization.get("id")).longValue();
            assertEquals("测试租户", organization.get("name"));
            assertEquals("T101", organization.get("code"));
            assertEquals(0L, ((Number) organization.get("pid")).longValue());
            assertEquals("/" + organizationId, organization.get("path"));
            assertEquals(true, organization.get("isleaf"));
            assertEquals(9999, ((Number) organization.get("frozen")).intValue());

            Map<String, Object> administrator = jdbcTemplate.queryForMap("""
                    SELECT a.ACCOUNT, a.PASSWORD, p.DEPT_ID, a.FROZEN
                    FROM tenant_101.SYS_ACCOUNT a JOIN tenant_101.SYS_ACCOUNT_PROFILE p ON p.ACCOUNT_ID=a.ID
                    WHERE a.ID = 1011
                    """);
            assertEquals("tenant-admin", administrator.get("account"));
            assertEquals(organizationId, ((Number) administrator.get("dept_id")).longValue());
            assertEquals(9999, ((Number) administrator.get("frozen")).intValue());
            assertTrue(BCryptUtils.checkpw("ChangeMe123!", administrator.get("password").toString()));
        }
    }

    @Test
    void routesPrivateAccountChangesToSelectedTenantOnly() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            createSchema(jdbcTemplate, "public");
            createSchema(jdbcTemplate, "tenant_101");
            createSchema(jdbcTemplate, "tenant_102");
            seed(jdbcTemplate);

            EvaConfig config = schemaConfig();
            TenantSchemaRouter router = new TenantSchemaRouter(dataSource, config,
                    tenantId -> "tenant_" + tenantId);
            TargetTenantExecutor executor = new TargetTenantExecutor(config, router);
            TenantPrivateAccountService service = new TenantPrivateAccountService(jdbcTemplate, executor);
            TransactionTemplate transaction = new TransactionTemplate(
                    new DataSourceTransactionManager(dataSource));

            ThreadUserHelper.runWithUser(platformAdministrator(), () -> transaction.executeWithoutResult(status -> {
                assertEquals(Set.of(1011L), service.incrementAllPermVersions(101L));
                service.pruneRoleResources(101L, Set.of(5001L));
            }));

            assertEquals(1L, value(jdbcTemplate, "tenant_101", 1011L));
            assertEquals(0L, value(jdbcTemplate, "tenant_102", 1011L));
            assertEquals(0L, value(jdbcTemplate, "public", 1011L));
            assertEquals(1, count(jdbcTemplate, "tenant_101", "SYS_ROLERES_REF"));
            assertEquals(2, count(jdbcTemplate, "tenant_102", "SYS_ROLERES_REF"));
            assertEquals(2, count(jdbcTemplate, "public", "SYS_ROLERES_REF"));

            ThreadUserHelper.runWithUser(platformAdministrator(), () -> transaction.executeWithoutResult(status ->
                    assertEquals(Set.of(1011L), service.softDeleteTenantAccounts(101L))));
            assertEquals(1011L, deleted(jdbcTemplate, "tenant_101", 1011L));
            assertEquals(0L, deleted(jdbcTemplate, "tenant_102", 1011L));
            assertEquals(0L, deleted(jdbcTemplate, "public", 1011L));
            assertEquals(0, count(jdbcTemplate, "tenant_101", "SYS_ROLERES_REF"));
            assertEquals(2, count(jdbcTemplate, "tenant_102", "SYS_ROLERES_REF"));
        }
    }

    @Test
    void standaloneUsesCurrentSchemaWithoutTargetRouting() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            createSchema(jdbcTemplate, "public");
            jdbcTemplate.update("INSERT INTO SYS_ACCOUNT(ID, DELETED, FROZEN, PERM_VER) VALUES (1, 0, 0, 0)");

            EvaConfig config = new EvaConfig();
            config.setMode("standalone");
            TenantProperties properties = new TenantProperties();
            properties.setEnable(false);
            config.setTenant(properties);
            TenantPrivateAccountService service = new TenantPrivateAccountService(jdbcTemplate,
                    new TargetTenantExecutor(config, null));

            assertEquals(Set.of(1L), service.incrementAllPermVersions(999L));
            assertEquals(1L, value(jdbcTemplate, "public", 1L));
        }
    }

    private void createSchema(JdbcTemplate jdbcTemplate, String schema) {
        if (!"public".equals(schema)) {
            jdbcTemplate.execute("CREATE SCHEMA " + schema);
        }
        jdbcTemplate.execute("""
                    CREATE TABLE %s.SYS_ACCOUNT(
                    ID BIGINT PRIMARY KEY, REVISION INTEGER, DELETED BIGINT DEFAULT 0,
                    FROZEN INTEGER DEFAULT 0, SORT DOUBLE PRECISION DEFAULT 0,
                    UTC_CREATE TIMESTAMP, ACCOUNT VARCHAR(100),
                    PASSWORD VARCHAR(200), NICK_NAME VARCHAR(100), PERM_VER BIGINT DEFAULT 0)
                """.formatted(schema));
        jdbcTemplate.execute("CREATE TABLE " + schema + ".SYS_ACCOUNT_PROFILE(ACCOUNT_ID BIGINT PRIMARY KEY "
                + "REFERENCES " + schema + ".SYS_ACCOUNT(ID), CODE VARCHAR(100), NAME VARCHAR(100), DEPT_ID BIGINT)");
        jdbcTemplate.execute("""
                CREATE TABLE %s.SYS_ORGANIZATION(
                    ID BIGINT PRIMARY KEY, REVISION INTEGER, DELETED BIGINT DEFAULT 0,
                    FROZEN INTEGER DEFAULT 0, SORT DOUBLE PRECISION DEFAULT 0,
                    UTC_CREATE TIMESTAMP, NAME VARCHAR(100), CODE VARCHAR(100),
                    PID BIGINT NOT NULL DEFAULT 0, PATH VARCHAR(1000), ISLEAF BOOLEAN DEFAULT TRUE)
                """.formatted(schema));
        jdbcTemplate.execute("""
                CREATE TABLE %s.SYS_ROLE(ID BIGINT PRIMARY KEY, DELETED BIGINT DEFAULT 0)
                """.formatted(schema));
        jdbcTemplate.execute("""
                CREATE TABLE %s.SYS_ROLERES_REF(
                    ROLE_ID BIGINT NOT NULL, RESOURCE_ID BIGINT NOT NULL,
                    PRIMARY KEY(ROLE_ID, RESOURCE_ID))
                """.formatted(schema));
        jdbcTemplate.execute("""
                CREATE TABLE %s.SYS_ROLEUSER_REF(ROLE_ID BIGINT, USER_ID BIGINT)
                """.formatted(schema));
        jdbcTemplate.execute("""
                CREATE TABLE %s.SYS_POSTUSER_REF(ID BIGINT, POST_ID BIGINT, USER_ID BIGINT)
                """.formatted(schema));
    }

    private void seed(JdbcTemplate jdbcTemplate) {
        for (String schema : Set.of("public", "tenant_101", "tenant_102")) {
            jdbcTemplate.update("INSERT INTO " + schema
                    + ".SYS_ACCOUNT(ID, DELETED, FROZEN, PERM_VER) VALUES (1011, 0, 0, 0)");
            jdbcTemplate.update("INSERT INTO " + schema + ".SYS_ROLE(ID, DELETED) VALUES (1, 0)");
            jdbcTemplate.update("INSERT INTO " + schema
                    + ".SYS_ROLERES_REF(ROLE_ID, RESOURCE_ID) VALUES (1, 5001), (1, 5002)");
        }
    }

    private long value(JdbcTemplate jdbcTemplate, String schema, long userId) {
        return jdbcTemplate.queryForObject("SELECT PERM_VER FROM " + schema + ".SYS_ACCOUNT WHERE ID = ?",
                Long.class, userId);
    }

    private long deleted(JdbcTemplate jdbcTemplate, String schema, long userId) {
        return jdbcTemplate.queryForObject("SELECT DELETED FROM " + schema + ".SYS_ACCOUNT WHERE ID = ?",
                Long.class, userId);
    }

    private int count(JdbcTemplate jdbcTemplate, String schema, String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + schema + "." + table,
                Integer.class);
    }

    private EvaConfig schemaConfig() {
        EvaConfig config = new EvaConfig();
        config.setMode("platform");
        TenantProperties properties = new TenantProperties();
        properties.setEnable(true);
        properties.setMode(TenantProperties.MODE_SCHEMA);
        properties.setCoreSchema("public");
        config.setTenant(properties);
        return config;
    }

    private ThreadUser platformAdministrator() {
        return new ThreadUser().setTenantId(0L).setRolesMap(Map.of(
                1L, new ThreadUser.GrantedRoles("Platform admin", "ROLE_ADMIN")));
    }
}
