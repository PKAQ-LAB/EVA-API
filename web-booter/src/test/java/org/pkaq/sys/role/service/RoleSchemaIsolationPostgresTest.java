package org.pkaq.sys.role.service;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchemaRouter;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 在真实 PostgreSQL 中验证本地角色引用与平台资源上限的 schema 隔离。
 *
 * @author PKAQ
 */
class RoleSchemaIsolationPostgresTest {

    @Test
    void readsTenantRoleRefsAndCoreResourceLimitInSeparateSteps() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            createTables(jdbcTemplate);
            seedData(jdbcTemplate);

            EvaConfig config = schemaConfig();
            TenantSchemaRouter router = new TenantSchemaRouter(dataSource, config, id -> "tenant_" + id);
            CoreSchemaExecutor coreExecutor = new CoreSchemaExecutor(jdbcTemplate, config);
            TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));

            transaction.executeWithoutResult(status -> {
                router.routeCurrentTransaction(101L);
                Set<Long> localRoleResources = new HashSet<>(jdbcTemplate.queryForList("""
                        SELECT DISTINCT rr.resource_id
                        FROM sys_roleuser_ref ru
                            JOIN sys_roleres_ref rr ON ru.role_id = rr.role_id
                        WHERE ru.user_id = ?
                        """, Long.class, 9L));
                Set<Long> effectiveLimit = coreExecutor.execute(template -> new HashSet<>(template.queryForList("""
                        SELECT resource_id FROM sys_tenant_resource WHERE tenant_id = ?
                        """, Long.class, 101L)));

                localRoleResources.retainAll(effectiveLimit);
                assertEquals(Set.of(10L), localRoleResources);
                assertEquals("tenant_101", jdbcTemplate.queryForObject(
                        "SELECT current_schema()", String.class));
            });

            transaction.executeWithoutResult(status -> {
                router.routeCurrentTransaction(102L);
                assertEquals(Set.of(20L), effectiveUserResources(jdbcTemplate, coreExecutor, 102L, 9L));
            });

            jdbcTemplate.update("DELETE FROM public.sys_tenant_resource WHERE tenant_id = 101");
            transaction.executeWithoutResult(status -> {
                router.routeCurrentTransaction(101L);
                assertEquals(Set.of(), effectiveUserResources(jdbcTemplate, coreExecutor, 101L, 9L));
            });
        }
    }

    private void createTables(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE SCHEMA tenant_101");
        jdbcTemplate.execute("CREATE SCHEMA tenant_102");
        jdbcTemplate.execute("CREATE TABLE public.sys_tenant_resource(tenant_id BIGINT, resource_id BIGINT)");
        jdbcTemplate.execute("CREATE TABLE public.sys_module_resources(id BIGINT PRIMARY KEY, main_id BIGINT)");
        jdbcTemplate.execute("CREATE TABLE tenant_101.sys_roleuser_ref(role_id BIGINT, user_id BIGINT)");
        jdbcTemplate.execute("CREATE TABLE tenant_101.sys_roleres_ref(role_id BIGINT, resource_id BIGINT)");
        jdbcTemplate.execute("CREATE TABLE tenant_102.sys_roleuser_ref(role_id BIGINT, user_id BIGINT)");
        jdbcTemplate.execute("CREATE TABLE tenant_102.sys_roleres_ref(role_id BIGINT, resource_id BIGINT)");
    }

    private void seedData(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO public.sys_module_resources(id, main_id) VALUES (?, ?)",
                List.of(new Object[]{10L, 1L}, new Object[]{20L, 2L}));
        jdbcTemplate.update("INSERT INTO public.sys_tenant_resource(tenant_id, resource_id) VALUES (101, 10)");
        jdbcTemplate.update("INSERT INTO public.sys_tenant_resource(tenant_id, resource_id) VALUES (102, 20)");
        jdbcTemplate.update("INSERT INTO tenant_101.sys_roleuser_ref(role_id, user_id) VALUES (5, 9)");
        jdbcTemplate.batchUpdate(
                "INSERT INTO tenant_101.sys_roleres_ref(role_id, resource_id) VALUES (?, ?)",
                List.of(new Object[]{5L, 10L}, new Object[]{5L, 20L}));
        jdbcTemplate.update("INSERT INTO tenant_102.sys_roleuser_ref(role_id, user_id) VALUES (5, 9)");
        jdbcTemplate.update("INSERT INTO tenant_102.sys_roleres_ref(role_id, resource_id) VALUES (5, 20)");
    }

    private Set<Long> effectiveUserResources(JdbcTemplate jdbcTemplate,
                                             CoreSchemaExecutor coreExecutor,
                                             Long tenantId,
                                             Long userId) {
        Set<Long> local = new HashSet<>(jdbcTemplate.queryForList("""
                SELECT DISTINCT rr.resource_id
                FROM sys_roleuser_ref ru
                    JOIN sys_roleres_ref rr ON ru.role_id = rr.role_id
                WHERE ru.user_id = ?
                """, Long.class, userId));
        Set<Long> limit = coreExecutor.execute(template -> new HashSet<>(template.queryForList("""
                SELECT resource_id FROM sys_tenant_resource WHERE tenant_id = ?
                """, Long.class, tenantId)));
        local.retainAll(limit);
        return local;
    }

    private EvaConfig schemaConfig() {
        EvaConfig config = new EvaConfig();
        TenantProperties properties = new TenantProperties();
        properties.setEnable(true);
        properties.setMode(TenantProperties.MODE_SCHEMA);
        properties.setCoreSchema("public");
        config.setTenant(properties);
        return config;
    }
}
