package org.pkaq.sys.tenant.service;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户角色授权数据库契约测试。
 *
 * @author PKAQ
 */
class TenantAuthorizationContractTest {

    @Test
    void migrationRetainsRevokedGrantsAndSeparatesSystemRole() throws Exception {
        String migration = resource("db/migration/V2__TENANT_ROLE_AUTHORIZATION.sql");

        assertTrue(migration.contains("ACTIVE BOOLEAN NOT NULL DEFAULT TRUE"));
        assertTrue(migration.contains("REVOKED_AT TIMESTAMP"));
        assertTrue(migration.contains("SYS_TENANT_PACKAGE_GRANT"));
        assertTrue(migration.contains("SYSTEM_ROLE VARCHAR(32)"));
        assertTrue(migration.contains("COALESCE(role.FROZEN, 0) <> 1"));
    }

    @Test
    void mapperBuildsUnionAndRevokesWithoutDeletingHistory() throws Exception {
        String mapper = resource("mapper/TenantAuthorization.xml");

        assertTrue(mapper.contains("UPDATE SYS_TENANT_ROLE SET ACTIVE = FALSE"));
        assertTrue(mapper.contains("UPDATE SYS_TENANT_PACKAGE_GRANT SET ACTIVE = FALSE"));
        assertTrue(mapper.contains("UNION"));
        assertTrue(mapper.contains("SYS_TENANT_PACKAGE_RESOURCE"));
        assertTrue(mapper.contains("SYSTEM_ROLE = 'TENANT_ADMIN'"));
    }

    private String resource(String path) throws Exception {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("缺少测试资源：" + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
