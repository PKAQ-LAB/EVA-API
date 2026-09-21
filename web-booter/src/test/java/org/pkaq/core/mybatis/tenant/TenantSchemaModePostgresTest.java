package org.pkaq.core.mybatis.tenant;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.sys.tenant.service.TenantSchemaProvisioner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 在隔离 PostgreSQL 中验证双运行模式的 schema 路由。
 *
 * @author PKAQ
 */
class TenantSchemaModePostgresTest {

    @Test
    void provisionsTrustedSchemaAndTenantMigrationIdempotently() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute("""
                    CREATE TABLE SYS_TENANT (
                        ID BIGINT PRIMARY KEY,
                        DELETED INTEGER DEFAULT 0,
                        FROZEN INTEGER DEFAULT 0
                    )
                    """);
            jdbcTemplate.update("INSERT INTO SYS_TENANT(ID) VALUES (101)");
            executeMigration(jdbcTemplate);

            EvaConfig config = schemaConfig();
            TrustedTenantSchemaResolver resolver = new TrustedTenantSchemaResolver(jdbcTemplate, config);
            TenantSchemaProvisioner provisioner = new TenantSchemaProvisioner(
                    jdbcTemplate, dataSource, config, resolver);
            TransactionTemplate transaction = new TransactionTemplate(
                    new DataSourceTransactionManager(dataSource));

            transaction.executeWithoutResult(status -> provisioner.provision(101L));
            transaction.executeWithoutResult(status -> provisioner.provision(101L));

            assertEquals("tenant_101", jdbcTemplate.queryForObject(
                    "SELECT SCHEMA_NAME FROM SYS_TENANT WHERE ID = 101", String.class));
            assertEquals(1, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = 'tenant_101'
                      AND table_name = 'eva_tenant_schema_version'
                    """, Integer.class));
            assertEquals(1, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = 'tenant_101' AND table_name = 'sys_user'
                    """, Integer.class));
            assertEquals(0, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema = 'tenant_101'
                      AND table_name IN (
                          'sys_user', 'sys_organization', 'sys_post', 'sys_dict', 'sys_dict_item',
                          'sys_role', 'sys_roleuser_ref', 'sys_roleres_ref', 'sys_postuser_ref')
                      AND column_name = 'tenant_id'
                    """, Integer.class));
            List<String> tenantTables = jdbcTemplate.queryForList("""
                    SELECT table_name FROM information_schema.tables
                    WHERE table_schema = 'tenant_101'
                      AND table_name IN (
                          'sys_user', 'sys_organization', 'sys_post', 'sys_dict', 'sys_dict_item',
                          'sys_role', 'sys_roleuser_ref', 'sys_roleres_ref', 'sys_postuser_ref')
                    ORDER BY table_name
                    """, String.class);
            assertEquals(9, tenantTables.size());
            assertEquals("tenant_101", resolver.resolve(101L));
        }
    }

    @Test
    void routesOnlyInsideTransactionAndRestoresCoreAccess() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute("CREATE SCHEMA tenant_101");
            jdbcTemplate.execute("CREATE TABLE public.route_probe(value VARCHAR(16))");
            jdbcTemplate.execute("CREATE TABLE tenant_101.route_probe(value VARCHAR(16))");
            jdbcTemplate.update("INSERT INTO public.route_probe(value) VALUES ('core')");
            jdbcTemplate.update("INSERT INTO tenant_101.route_probe(value) VALUES ('tenant')");

            EvaConfig config = schemaConfig();
            TenantSchemaRouter router = new TenantSchemaRouter(dataSource, config, id -> "tenant_101");
            CoreSchemaExecutor coreExecutor = new CoreSchemaExecutor(jdbcTemplate, config);
            assertThrows(IllegalStateException.class, () -> router.routeCurrentTransaction(101L));
            assertThrows(IllegalStateException.class, () -> coreExecutor.execute(template -> "core"));

            TransactionTemplate transaction = new TransactionTemplate(
                    new DataSourceTransactionManager(dataSource));
            transaction.executeWithoutResult(status -> {
                router.routeCurrentTransaction(101L);
                assertEquals("tenant", jdbcTemplate.queryForObject(
                        "SELECT value FROM route_probe", String.class));
                assertEquals("core", coreExecutor.execute(template -> template.queryForObject(
                        "SELECT value FROM route_probe", String.class)));
                assertEquals("tenant", jdbcTemplate.queryForObject(
                        "SELECT value FROM route_probe", String.class));
                String previous = router.routeCurrentTransaction(101L);
                router.restoreCurrentTransaction(previous);
                assertEquals("tenant", jdbcTemplate.queryForObject(
                        "SELECT value FROM route_probe", String.class));
            });

            assertEquals("core", jdbcTemplate.queryForObject(
                    "SELECT value FROM route_probe", String.class));
        }
    }

    @Test
    void standaloneBypassesTenantContextAndSchemaLookup() {
        EvaConfig config = new EvaConfig();
        TenantProperties properties = new TenantProperties();
        properties.setEnable(false);
        properties.setMode(TenantProperties.MODE_SCHEMA);
        properties.setStandaloneSchema("eva");
        config.setTenant(properties);

        TrustedTenantSchemaResolver resolver = new TrustedTenantSchemaResolver(null, config);
        assertFalse(properties.isSchemaMode());
        assertEquals("eva", resolver.resolve(null));
        new TenantSchemaRouter(null, config, resolver).routeCurrentTransaction(null);
    }

    @Test
    void rejectsUntrustedSchemaNames() {
        EvaConfig config = schemaConfig();
        TrustedTenantSchemaResolver resolver = new TrustedTenantSchemaResolver(null, config);
        assertThrows(IllegalStateException.class, () -> resolver.validate("public"));
        assertThrows(IllegalStateException.class, () -> resolver.validate("tenant_1;drop schema public"));
        assertTrue(config.getTenant().isSchemaMode());
    }

    @Test
    void rejectsTargetTenantOverrideWithoutPlatformAdministrator() {
        EvaConfig config = schemaConfig();
        config.setMode("platform");
        TargetTenantExecutor executor = new TargetTenantExecutor(config, null);
        assertThrows(SecurityException.class, () -> executor.execute(101L, () -> "denied"));
    }

    private EvaConfig schemaConfig() {
        EvaConfig config = new EvaConfig();
        TenantProperties properties = new TenantProperties();
        properties.setEnable(true);
        properties.setMode(TenantProperties.MODE_SCHEMA);
        properties.setCoreSchema("public");
        properties.setStandaloneSchema("eva");
        properties.setPrefix("tenant_");
        config.setTenant(properties);
        return config;
    }

    private void executeMigration(JdbcTemplate jdbcTemplate) throws IOException {
        ClassPathResource migration = new ClassPathResource("db/migration/V3__TENANT_SCHEMA_MODE.sql");
        String script = StreamUtils.copyToString(migration.getInputStream(), StandardCharsets.UTF_8);
        for (String statement : script.split("--#")) {
            if (!statement.isBlank()) {
                jdbcTemplate.execute(statement);
            }
        }
    }
}
