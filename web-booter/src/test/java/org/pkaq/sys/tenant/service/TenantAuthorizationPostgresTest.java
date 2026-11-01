package org.pkaq.sys.tenant.service;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.pkaq.test.PostgresInstanceEvidence;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 在新初始化结构的隔离 PostgreSQL 中验证授权撤销与租户边界。
 *
 * @author PKAQ
 */
class TenantAuthorizationPostgresTest {

    @Test
    void enforcesUnionRevocationTenantBoundaryCropAndPermissionVersionTransaction() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                .setServerConfig("listen_addresses", "127.0.0.1").setPort(0).start();
             Connection connection = postgres.getPostgresDatabase().getConnection()) {
            PostgresInstanceEvidence.capture(postgres.getPostgresDatabase(), "init-authorization");
            createCurrentSchema(connection);
            insertCurrentFixtureData(connection);
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
            execute(connection, "UPDATE SYS_ACCOUNT SET PERM_VER = PERM_VER + 1 WHERE TENANT_ID = 101");
            assertEquals(2, longValue(connection,
                    "SELECT SUM(PERM_VER) FROM SYS_ACCOUNT WHERE TENANT_ID = 101"));
            assertEquals(0, longValue(connection,
                    "SELECT SUM(PERM_VER) FROM SYS_ACCOUNT WHERE TENANT_ID = 102"));
            connection.rollback();
            connection.setAutoCommit(true);
            assertEquals(0, longValue(connection, "SELECT SUM(PERM_VER) FROM SYS_ACCOUNT"));
        }
    }

    private void createCurrentSchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(new ClassPathResource("db/migration/V1__INITDB.sql")
                    .getContentAsString(StandardCharsets.UTF_8));
        }
    }

    private void insertPackageFixtures(Connection connection) throws Exception {
        execute(connection, """
                INSERT INTO SYS_TENANT_PACKAGE(ID,CODE,NAME) VALUES
                    (601,'fixture601','测试套餐601'),(602,'fixture602','测试套餐602'),(603,'fixture603','测试套餐603');
                INSERT INTO SYS_TENANT_PACKAGE_RESOURCE(PACKAGE_ID, RESOURCE_ID)
                    VALUES (601, 502), (602, 503), (603, 504);
                INSERT INTO SYS_TENANT_PACKAGE_GRANT(TENANT_ID, PACKAGE_ID, ACTIVE, GRANTED_AT)
                    VALUES (101, 601, TRUE, CURRENT_TIMESTAMP),
                           (101, 602, TRUE, CURRENT_TIMESTAMP),
                           (102, 603, TRUE, CURRENT_TIMESTAMP);
                INSERT INTO SYS_ACCOUNT(ID, TENANT_ID) VALUES (701, 101), (702, 101), (703, 102);
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

    private void insertCurrentFixtureData(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO SYS_TENANT(ID, CODE, ADMIN_ID) VALUES (101, 'alpha', 201), (102, 'beta', 202);
                    INSERT INTO SYS_ROLE(ID, TENANT_ID, CODE, NAME) VALUES
                        (301, 0, 'ROLE_TEMPLATE', '测试模板'),
                        (302, 101, 'ROLE_LOCAL', '非平台模板');
                    INSERT INTO SYS_ROLE(ID, TENANT_ID, CODE, SYSTEM_ROLE) VALUES
                        (901, 101, 'TENANT_ADMIN_101', 'TENANT_ADMIN'),
                        (902, 102, 'TENANT_ADMIN_102', 'TENANT_ADMIN');
                    INSERT INTO SYS_TENANT_ROLE(ID, TENANT_ID, ROLE_ID, ACTIVE) VALUES
                        (401, 101, 301, TRUE), (402, 101, 302, FALSE);
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

}
