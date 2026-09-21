package org.pkaq.sys.tenant.service;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 在隔离 PostgreSQL 进程中验证租户授权迁移。
 *
 * @author PKAQ
 */
class TenantAuthorizationPostgresTest {

    @Test
    void migratesHistoryFailClosedAndBackfillsTenantAdministrators() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start();
             Connection connection = postgres.getPostgresDatabase().getConnection()) {
            createLegacySchema(connection);
            insertLegacyData(connection);

            new ResourceDatabasePopulator(
                    new ClassPathResource("db/migration/V2__TENANT_ROLE_AUTHORIZATION.sql"))
                    .execute(postgres.getPostgresDatabase());

            assertTrue(booleanValue(connection,
                    "SELECT ACTIVE FROM SYS_TENANT_ROLE WHERE TENANT_ID = 101 AND ROLE_ID = 301"));
            assertFalse(booleanValue(connection,
                    "SELECT ACTIVE FROM SYS_TENANT_ROLE WHERE TENANT_ID = 101 AND ROLE_ID = 302"));
            assertEquals("MIGRATION_REJECTED_INVALID_TEMPLATE", stringValue(connection,
                    "SELECT STATUS_REASON FROM SYS_TENANT_ROLE WHERE TENANT_ID = 101 AND ROLE_ID = 302"));
            assertEquals(2, longValue(connection,
                    "SELECT COUNT(*) FROM SYS_ROLE WHERE SYSTEM_ROLE = 'TENANT_ADMIN'"));
            assertEquals(2, longValue(connection,
                    "SELECT COUNT(*) FROM SYS_ROLEUSER_REF ru JOIN SYS_ROLE r ON r.ID = ru.ROLE_ID "
                            + "WHERE r.SYSTEM_ROLE = 'TENANT_ADMIN'"));
            assertEquals(1, longValue(connection,
                    "SELECT COUNT(*) FROM SYS_ROLERES_REF rr JOIN SYS_ROLE r ON r.ID = rr.ROLE_ID "
                            + "WHERE r.TENANT_ID = 101 AND r.SYSTEM_ROLE = 'TENANT_ADMIN' "
                            + "AND rr.RESOURCE_ID = 501"));

            // 第二次执行验证所有回填语句幂等。
            new ResourceDatabasePopulator(
                    new ClassPathResource("db/migration/V2__TENANT_ROLE_AUTHORIZATION.sql"))
                    .execute(postgres.getPostgresDatabase());
            assertEquals(2, longValue(connection,
                    "SELECT COUNT(*) FROM SYS_ROLE WHERE SYSTEM_ROLE = 'TENANT_ADMIN'"));
        }
    }

    @Test
    void enforcesUnionRevocationTenantBoundaryCropAndPermissionVersionTransaction() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start();
             Connection connection = postgres.getPostgresDatabase().getConnection()) {
            createLegacySchema(connection);
            insertLegacyData(connection);
            new ResourceDatabasePopulator(
                    new ClassPathResource("db/migration/V2__TENANT_ROLE_AUTHORIZATION.sql"))
                    .execute(postgres.getPostgresDatabase());
            insertPackageFixtures(connection);

            rebuildResources(connection, 101);
            rebuildResources(connection, 102);
            assertEquals(Set.of(501L, 502L, 503L), resourceIds(connection, 101));
            assertEquals(Set.of(504L), resourceIds(connection, 102));

            execute(connection, "UPDATE SYS_TENANT_ROLE SET ACTIVE = FALSE, REVOKED_AT = CURRENT_TIMESTAMP "
                    + "WHERE TENANT_ID = 101 AND ROLE_ID = 301");
            rebuildResources(connection, 101);
            assertEquals(Set.of(502L, 503L), resourceIds(connection, 101));

            // 999 不在派生能力中，模拟服务端 containsAll 校验必须拒绝扩权。
            assertEquals(1, longValue(connection,
                    "SELECT COUNT(*) FROM SYS_TENANT_RESOURCE WHERE TENANT_ID = 101 "
                            + "AND RESOURCE_ID IN (502, 999)"));
            execute(connection, "DELETE FROM SYS_TENANT_RESOURCE "
                    + "WHERE TENANT_ID = 101 AND RESOURCE_ID NOT IN (502)");
            refreshAdminResources(connection, 101);
            assertEquals(Set.of(502L), adminResourceIds(connection, 101));

            connection.setAutoCommit(false);
            execute(connection, "UPDATE SYS_USER SET PERM_VER = PERM_VER + 1 WHERE TENANT_ID = 101");
            assertEquals(2, longValue(connection,
                    "SELECT SUM(PERM_VER) FROM SYS_USER WHERE TENANT_ID = 101"));
            assertEquals(0, longValue(connection,
                    "SELECT SUM(PERM_VER) FROM SYS_USER WHERE TENANT_ID = 102"));
            connection.rollback();
            connection.setAutoCommit(true);
            assertEquals(0, longValue(connection, "SELECT SUM(PERM_VER) FROM SYS_USER"));
        }
    }

    private void createLegacySchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE SYS_TENANT(
                        ID BIGINT PRIMARY KEY, DELETED BIGINT DEFAULT 0, FROZEN INTEGER DEFAULT 0, ADMIN_ID BIGINT);
                    CREATE TABLE SYS_ROLE(
                        ID BIGINT PRIMARY KEY, REVISION INTEGER, DELETED BIGINT DEFAULT 0, FROZEN INTEGER DEFAULT 0,
                        SORT DOUBLE PRECISION DEFAULT 0, TENANT_ID BIGINT DEFAULT 0, UTC_CREATE TIMESTAMP,
                        CODE VARCHAR(100), NAME VARCHAR(100), DATA_SCOPE VARCHAR(50));
                    CREATE UNIQUE INDEX UK_SYS_ROLE_TENANT_CODE
                        ON SYS_ROLE(TENANT_ID, CODE) WHERE DELETED = 0;
                    CREATE TABLE SYS_TENANT_ROLE(
                        ID BIGINT PRIMARY KEY, TENANT_ID BIGINT NOT NULL, ROLE_ID BIGINT NOT NULL,
                        CONSTRAINT UK_SYS_TENANT_ROLE UNIQUE(TENANT_ID, ROLE_ID));
                    CREATE TABLE SYS_ROLEUSER_REF(
                        ROLE_ID BIGINT NOT NULL, USER_ID BIGINT NOT NULL,
                        PRIMARY KEY(ROLE_ID, USER_ID));
                    CREATE TABLE SYS_ROLERES_REF(
                        ROLE_ID BIGINT NOT NULL, RESOURCE_ID BIGINT NOT NULL,
                        PRIMARY KEY(ROLE_ID, RESOURCE_ID));
                    CREATE TABLE SYS_TENANT_RESOURCE(
                        TENANT_ID BIGINT NOT NULL, RESOURCE_ID BIGINT NOT NULL,
                        PRIMARY KEY(TENANT_ID, RESOURCE_ID));
                    CREATE TABLE SYS_TENANT_PACKAGE(
                        ID BIGINT PRIMARY KEY, DELETED BIGINT DEFAULT 0, FROZEN INTEGER DEFAULT 0);
                    CREATE TABLE SYS_TENANT_PACKAGE_RESOURCE(
                        PACKAGE_ID BIGINT NOT NULL, RESOURCE_ID BIGINT NOT NULL,
                        PRIMARY KEY(PACKAGE_ID, RESOURCE_ID));
                    CREATE TABLE SYS_USER(
                        ID BIGINT PRIMARY KEY, TENANT_ID BIGINT NOT NULL, DELETED BIGINT DEFAULT 0,
                        PERM_VER BIGINT NOT NULL DEFAULT 0);
                    """);
        }
    }

    private void insertPackageFixtures(Connection connection) throws Exception {
        execute(connection, """
                INSERT INTO SYS_TENANT_PACKAGE(ID) VALUES (601), (602), (603);
                INSERT INTO SYS_TENANT_PACKAGE_RESOURCE(PACKAGE_ID, RESOURCE_ID)
                    VALUES (601, 502), (602, 503), (603, 504);
                INSERT INTO SYS_TENANT_PACKAGE_GRANT(TENANT_ID, PACKAGE_ID, ACTIVE, GRANTED_AT)
                    VALUES (101, 601, TRUE, CURRENT_TIMESTAMP),
                           (101, 602, TRUE, CURRENT_TIMESTAMP),
                           (102, 603, TRUE, CURRENT_TIMESTAMP);
                INSERT INTO SYS_USER(ID, TENANT_ID) VALUES (701, 101), (702, 101), (703, 102);
                """);
    }

    private void rebuildResources(Connection connection, long tenantId) throws Exception {
        execute(connection, "DELETE FROM SYS_TENANT_RESOURCE WHERE TENANT_ID = " + tenantId);
        execute(connection, """
                INSERT INTO SYS_TENANT_RESOURCE(TENANT_ID, RESOURCE_ID)
                SELECT %1$d, RESOURCE_ID FROM (
                    SELECT rr.RESOURCE_ID
                    FROM SYS_TENANT_ROLE tr JOIN SYS_ROLERES_REF rr ON rr.ROLE_ID = tr.ROLE_ID
                    WHERE tr.TENANT_ID = %1$d AND tr.ACTIVE = TRUE
                    UNION
                    SELECT pr.RESOURCE_ID
                    FROM SYS_TENANT_PACKAGE_GRANT pg
                    JOIN SYS_TENANT_PACKAGE p ON p.ID = pg.PACKAGE_ID
                    JOIN SYS_TENANT_PACKAGE_RESOURCE pr ON pr.PACKAGE_ID = pg.PACKAGE_ID
                    WHERE pg.TENANT_ID = %1$d AND pg.ACTIVE = TRUE
                      AND COALESCE(p.DELETED, 0) = 0 AND COALESCE(p.FROZEN, 0) <> 1
                ) capability
                """.formatted(tenantId));
    }

    private void refreshAdminResources(Connection connection, long tenantId) throws Exception {
        execute(connection, "DELETE FROM SYS_ROLERES_REF rr USING SYS_ROLE r "
                + "WHERE rr.ROLE_ID = r.ID AND r.TENANT_ID = " + tenantId
                + " AND r.SYSTEM_ROLE = 'TENANT_ADMIN'");
        execute(connection, "INSERT INTO SYS_ROLERES_REF(ROLE_ID, RESOURCE_ID) "
                + "SELECT r.ID, tr.RESOURCE_ID FROM SYS_ROLE r JOIN SYS_TENANT_RESOURCE tr "
                + "ON tr.TENANT_ID = r.TENANT_ID WHERE r.TENANT_ID = " + tenantId
                + " AND r.SYSTEM_ROLE = 'TENANT_ADMIN'");
    }

    private Set<Long> resourceIds(Connection connection, long tenantId) throws Exception {
        return idSet(connection, "SELECT RESOURCE_ID FROM SYS_TENANT_RESOURCE WHERE TENANT_ID = " + tenantId);
    }

    private Set<Long> adminResourceIds(Connection connection, long tenantId) throws Exception {
        return idSet(connection, "SELECT rr.RESOURCE_ID FROM SYS_ROLERES_REF rr JOIN SYS_ROLE r ON r.ID = rr.ROLE_ID "
                + "WHERE r.TENANT_ID = " + tenantId + " AND r.SYSTEM_ROLE = 'TENANT_ADMIN'");
    }

    private Set<Long> idSet(Connection connection, String sql) throws Exception {
        java.util.HashSet<Long> resultIds = new java.util.HashSet<>();
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                resultIds.add(result.getLong(1));
            }
        }
        return resultIds;
    }

    private void execute(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void insertLegacyData(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO SYS_TENANT(ID, ADMIN_ID) VALUES (101, 201), (102, 202);
                    INSERT INTO SYS_ROLE(ID, TENANT_ID, CODE, NAME) VALUES
                        (301, 0, 'ROLE_ADMIN', 'Platform admin'),
                        (302, 101, 'ROLE_LOCAL', 'Illegal local template');
                    INSERT INTO SYS_TENANT_ROLE(ID, TENANT_ID, ROLE_ID) VALUES
                        (401, 101, 301), (402, 101, 302);
                    INSERT INTO SYS_ROLERES_REF(ROLE_ID, RESOURCE_ID) VALUES (301, 501);
                    INSERT INTO SYS_TENANT_RESOURCE(TENANT_ID, RESOURCE_ID) VALUES (101, 501);
                    UPDATE SYS_ROLE SET FROZEN = 9999 WHERE ID = 301;
                    """);
        }
    }

    private long longValue(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getLong(1);
        }
    }

    private boolean booleanValue(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getBoolean(1);
        }
    }

    private String stringValue(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getString(1);
        }
    }
}
