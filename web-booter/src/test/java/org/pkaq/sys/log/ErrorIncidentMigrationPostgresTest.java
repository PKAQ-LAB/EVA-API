package org.pkaq.sys.log;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 错误事件 V8 数据库迁移验证。
 *
 * @author PKAQ
 */
class ErrorIncidentMigrationPostgresTest {

    @Test
    void migratesLegacyErrorsAndCreatesAggregationConstraint() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(postgres.getPostgresDatabase());
            jdbcTemplate.execute("""
                    CREATE TABLE LOG_ERROR (
                        ID BIGINT PRIMARY KEY, REQUEST_TIME VARCHAR(32), IP VARCHAR(64),
                        CLASS_NAME VARCHAR(300), METHOD VARCHAR(200), PARAMS TEXT, EX_DESC TEXT,
                        LOGIN_USER VARCHAR(100), TENANT_ID BIGINT NOT NULL DEFAULT 0, SPEND_TIME VARCHAR(32)
                    )
                    """);
            jdbcTemplate.update("""
                    INSERT INTO LOG_ERROR(ID, TENANT_ID, EX_DESC)
                    VALUES (1, 0, 'legacy stack')
                    """);

            executeMigration(jdbcTemplate);

            assertEquals(1L, jdbcTemplate.queryForObject(
                    "SELECT OCCURRENCE_COUNT FROM LOG_ERROR WHERE ID = 1", Long.class));
            assertEquals("legacy stack", jdbcTemplate.queryForObject(
                    "SELECT SUMMARY FROM LOG_ERROR WHERE ID = 1", String.class));
            assertEquals(0, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_name = 'log_error' AND column_name = 'ex_desc'
                    """, Integer.class));
            assertEquals(1, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM pg_indexes
                    WHERE tablename = 'log_error' AND indexname = 'uk_log_error_tenant_fingerprint'
                    """, Integer.class));
        }
    }

    private void executeMigration(JdbcTemplate jdbcTemplate) throws Exception {
        ClassPathResource migration = new ClassPathResource(
                "db/migration/V8__ERROR_INCIDENT_AGGREGATION.sql");
        String script = StreamUtils.copyToString(migration.getInputStream(), StandardCharsets.UTF_8);
        for (String statement : script.split("--#")) {
            if (!statement.isBlank()) {
                jdbcTemplate.execute(statement);
            }
        }
    }
}
