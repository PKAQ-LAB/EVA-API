package org.pkaq.sys.log;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 业务日志 V4 数据库迁移验证。
 *
 * @author PKAQ
 */
class BusinessLogMigrationPostgresTest {

    @Test
    void migratesExistingLogTablesIdempotently() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(postgres.getPostgresDatabase());
            createLegacyTables(jdbcTemplate);
            jdbcTemplate.update("INSERT INTO LOG_BIZ(ID, OPERATE_DATETIME) VALUES (1, '2026-01-01 00:00:00')");
            jdbcTemplate.update("INSERT INTO LOG_ERROR(ID) VALUES (1)");
            jdbcTemplate.update("INSERT INTO SYS_LOGIN_LOG(ID) VALUES (1)");

            executeMigration(jdbcTemplate);
            executeMigration(jdbcTemplate);

            assertEquals(0, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM LOG_BIZ WHERE TENANT_ID IS NULL", Integer.class));
            assertEquals(0, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM LOG_ERROR WHERE TENANT_ID IS NULL", Integer.class));
            assertEquals(0, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM SYS_LOGIN_LOG WHERE TENANT_ID IS NULL", Integer.class));
            assertEquals(1, jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name = 'log_biz_archive'
                    """, Integer.class));
        }
    }

    private void createLegacyTables(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE LOG_BIZ (
                    ID BIGINT PRIMARY KEY, TENANT_ID BIGINT, OPERATOR VARCHAR(100),
                    OPERATE_TYPE VARCHAR(32), OPERATE_DATETIME VARCHAR(32), SPEND_TIME VARCHAR(32),
                    DESCRIPTION VARCHAR(1000), M_CODE VARCHAR(100), B_ID VARCHAR(100),
                    CLASS_NAME VARCHAR(300), METHOD VARCHAR(200), PARAMS TEXT, RESPONSE TEXT,
                    DEVICE VARCHAR(64), VERSION VARCHAR(64)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE LOG_ERROR (
                    ID BIGINT PRIMARY KEY, REQUEST_TIME VARCHAR(32), IP VARCHAR(64),
                    CLASS_NAME VARCHAR(300), METHOD VARCHAR(200), PARAMS TEXT, EX_DESC TEXT,
                    LOGIN_USER VARCHAR(100), TENANT_ID BIGINT, SPEND_TIME VARCHAR(32)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE SYS_LOGIN_LOG (
                    ID BIGINT PRIMARY KEY, TENANT_ID BIGINT, USER_ID BIGINT, ACCOUNT VARCHAR(100),
                    LOGIN_TYPE VARCHAR(32) NOT NULL DEFAULT 'PASSWORD',
                    SUCCESS BOOLEAN NOT NULL DEFAULT FALSE, UTC_CREATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    private void executeMigration(JdbcTemplate jdbcTemplate) throws Exception {
        ClassPathResource migration = new ClassPathResource("db/migration/V4__BUSINESS_LOG_ARCHIVE.sql");
        String script = StreamUtils.copyToString(migration.getInputStream(), StandardCharsets.UTF_8);
        for (String statement : script.split("--#")) {
            if (!statement.isBlank()) {
                jdbcTemplate.execute(statement);
            }
        }
    }
}
