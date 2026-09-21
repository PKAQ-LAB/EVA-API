package org.pkaq.core.mybatis.tenant;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用真实 Spring 代理链验证事务和租户 schema 路由顺序。
 *
 * @author PKAQ
 */
class TenantSchemaAdvisorOrderPostgresTest {
    private static final String TEST_PROFILE = "tenant-schema-advisor-order-test";

    @Test
    void startsTransactionBeforeRoutingAndRestoresSearchPathAfterInvocation() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute("CREATE SCHEMA tenant_101");
            ProxyTestConfiguration.dataSource = dataSource;

            try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
                context.getEnvironment().setActiveProfiles(TEST_PROFILE);
                context.register(ProxyTestConfiguration.class);
                context.refresh();
                ProbeService service = context.getBean(ProbeService.class);
                ProbeResult[] result = new ProbeResult[1];
                ThreadUser user = new ThreadUser().setTenantId(101L);
                ThreadUserHelper.runWithUser(user, () -> result[0] = service.probe());

                assertTrue(result[0].transactionActive());
                assertEquals("tenant_101", result[0].schema());
                assertEquals("public", jdbcTemplate.queryForObject("SELECT current_schema()", String.class));
            } finally {
                ProxyTestConfiguration.dataSource = null;
            }
        }
    }

    @TestConfiguration
    @Profile(TEST_PROFILE)
    @EnableAspectJAutoProxy
    @EnableTransactionManagement(order = TenantRoutingOrder.TRANSACTION)
    static class ProxyTestConfiguration {
        private static DataSource dataSource;

        @Bean
        DataSource dataSource() {
            return dataSource;
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource source) {
            return new DataSourceTransactionManager(source);
        }

        @Bean
        EvaConfig evaConfig() {
            EvaConfig config = new EvaConfig();
            TenantProperties tenant = new TenantProperties();
            tenant.setEnable(true);
            tenant.setMode(TenantProperties.MODE_SCHEMA);
            tenant.setCoreSchema("public");
            config.setTenant(tenant);
            return config;
        }

        @Bean
        TenantSchemaResolver tenantSchemaResolver() {
            return tenantId -> "tenant_" + tenantId;
        }

        @Bean
        TenantSchemaRouter tenantSchemaRouter(DataSource source,
                                               EvaConfig config,
                                               TenantSchemaResolver resolver) {
            return new TenantSchemaRouter(source, config, resolver);
        }

        @Bean
        TenantSchemaTransactionalAspect tenantSchemaTransactionalAspect(TenantSchemaRouter router) {
            return new TenantSchemaTransactionalAspect(router);
        }

        @Bean
        ProbeService probeService(JdbcTemplate template) {
            return new ProbeService(template);
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource source) {
            return new JdbcTemplate(source);
        }
    }

    static class ProbeService {
        private final JdbcTemplate jdbcTemplate;

        ProbeService(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Transactional(readOnly = true)
        @TenantSchema
        public ProbeResult probe() {
            return new ProbeResult(
                    TransactionSynchronizationManager.isActualTransactionActive(),
                    jdbcTemplate.queryForObject("SELECT current_schema()", String.class));
        }
    }

    record ProbeResult(boolean transactionActive, String schema) {
    }
}
