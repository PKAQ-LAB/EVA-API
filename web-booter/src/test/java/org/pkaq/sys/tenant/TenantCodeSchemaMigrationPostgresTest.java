package org.pkaq.sys.tenant;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户 code schema 数据保全迁移验证。
 *
 * @author PKAQ
 */
class TenantCodeSchemaMigrationPostgresTest {

    private static final String MIGRATION = "db/migration/V13__TENANT_CODE_SCHEMA.sql";

    @Test
    void renamesLegacySchemaAndPreservesObjectsDataSequenceRelationsAndPrivileges() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate database = new JdbcTemplate(postgres.getPostgresDatabase());
            createTenantTable(database);
            database.execute("CREATE ROLE tenant_reader");
            database.execute("CREATE SCHEMA tenant_101");
            database.execute("CREATE TABLE tenant_101.parent(id BIGSERIAL PRIMARY KEY, value VARCHAR(32))");
            database.execute("CREATE TABLE tenant_101.child(parent_id BIGINT REFERENCES tenant_101.parent(id))");
            database.update("INSERT INTO tenant_101.parent(value) VALUES ('sentinel')");
            database.update("INSERT INTO tenant_101.child(parent_id) VALUES (1)");
            database.execute("GRANT USAGE ON SCHEMA tenant_101 TO tenant_reader");
            database.execute("GRANT SELECT ON tenant_101.parent TO tenant_reader");
            database.execute("CREATE SCHEMA tenant_beta");
            database.execute("CREATE TABLE tenant_beta.sentinel(value VARCHAR(16))");
            database.update("INSERT INTO tenant_beta.sentinel(value) VALUES ('ready')");
            database.execute("CREATE SCHEMA tenant_acme1");
            database.execute("CREATE TABLE tenant_acme1.sentinel(value VARCHAR(16))");
            database.update("INSERT INTO tenant_acme1.sentinel(value) VALUES ('canonical')");
            database.update("""
                    INSERT INTO SYS_TENANT(ID, DELETED, CODE, SCHEMA_NAME) VALUES
                    (0, 0, 'eva', NULL),
                    (1, 0, 'acme1', 'tenant_acme1'),
                    (101, 0, 'acme', 'tenant_101'),
                    (202, 1, 'beta', 'tenant_beta')
                    """);

            Long parentOid = database.queryForObject("SELECT 'tenant_101.parent'::regclass::oid", Long.class);
            Long sequenceOid = database.queryForObject(
                    "SELECT 'tenant_101.parent_id_seq'::regclass::oid", Long.class);
            String schemaOwner = database.queryForObject("""
                    SELECT role.rolname
                    FROM pg_namespace namespace
                    JOIN pg_roles role ON role.oid = namespace.nspowner
                    WHERE namespace.nspname = 'tenant_101'
                    """, String.class);
            Integer objectCount = database.queryForObject("""
                    SELECT COUNT(*) FROM pg_class relation
                    JOIN pg_namespace namespace ON namespace.oid = relation.relnamespace
                    WHERE namespace.nspname = 'tenant_101'
                    """, Integer.class);

            migrateInTransaction(postgres, MIGRATION);
            migrateInTransaction(postgres, MIGRATION);

            assertFalse(schemaExists(database, "tenant_101"));
            assertTrue(schemaExists(database, "tenant_acme"));
            assertEquals("tenant_acme", database.queryForObject(
                    "SELECT SCHEMA_NAME FROM SYS_TENANT WHERE ID = 101", String.class));
            assertEquals("tenant_beta", database.queryForObject(
                    "SELECT SCHEMA_NAME FROM SYS_TENANT WHERE ID = 202", String.class));
            assertEquals("tenant_acme1", database.queryForObject(
                    "SELECT SCHEMA_NAME FROM SYS_TENANT WHERE ID = 1", String.class));
            assertEquals("canonical", database.queryForObject(
                    "SELECT value FROM tenant_acme1.sentinel", String.class));
            assertEquals("sentinel", database.queryForObject(
                    "SELECT value FROM tenant_acme.parent WHERE id = 1", String.class));
            assertEquals(1, database.queryForObject(
                    "SELECT COUNT(*) FROM tenant_acme.child WHERE parent_id = 1", Integer.class));
            assertEquals(parentOid, database.queryForObject(
                    "SELECT 'tenant_acme.parent'::regclass::oid", Long.class));
            assertEquals(sequenceOid, database.queryForObject(
                    "SELECT 'tenant_acme.parent_id_seq'::regclass::oid", Long.class));
            assertEquals(schemaOwner, database.queryForObject("""
                    SELECT role.rolname
                    FROM pg_namespace namespace
                    JOIN pg_roles role ON role.oid = namespace.nspowner
                    WHERE namespace.nspname = 'tenant_acme'
                    """, String.class));
            assertEquals(objectCount, database.queryForObject("""
                    SELECT COUNT(*) FROM pg_class relation
                    JOIN pg_namespace namespace ON namespace.oid = relation.relnamespace
                    WHERE namespace.nspname = 'tenant_acme'
                    """, Integer.class));
            assertEquals(2L, database.queryForObject(
                    "SELECT nextval('tenant_acme.parent_id_seq')", Long.class));
            assertTrue(Boolean.TRUE.equals(database.queryForObject(
                    "SELECT has_schema_privilege('tenant_reader', 'tenant_acme', 'USAGE')", Boolean.class)));
            assertTrue(Boolean.TRUE.equals(database.queryForObject(
                    "SELECT has_table_privilege('tenant_reader', 'tenant_acme.parent', 'SELECT')", Boolean.class)));
            assertEquals(1, database.queryForObject("""
                    SELECT COUNT(*) FROM pg_constraint
                    WHERE conrelid = 'tenant_acme.child'::regclass AND contype = 'f'
                    """, Integer.class));
            assertThrows(Exception.class,
                    () -> database.execute("INSERT INTO SYS_TENANT(ID, DELETED, CODE) VALUES (303, 1, 'beta')"));
            assertThrows(Exception.class,
                    () -> database.queryForObject("SELECT EVA_PROVISION_TENANT_SCHEMA('tenant_acme')", Object.class));
        }
    }

    @Test
    void rejectsUnsafeMetadataAndRollsEveryRenameBack() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate database = new JdbcTemplate(postgres.getPostgresDatabase());
            createTenantTable(database);
            database.execute("CREATE SCHEMA tenant_11");
            database.execute("CREATE TABLE tenant_11.sentinel(value VARCHAR(16))");
            database.update("INSERT INTO tenant_11.sentinel(value) VALUES ('safe')");
            database.update("""
                    INSERT INTO SYS_TENANT(ID, DELETED, CODE, SCHEMA_NAME)
                    VALUES (11, 0, 'acme', 'tenant_11')
                    """);

            database.update("UPDATE SYS_TENANT SET CODE = 'ACME' WHERE ID = 11");
            assertMigrationFails(postgres);
            database.update("UPDATE SYS_TENANT SET CODE = '' WHERE ID = 11");
            assertMigrationFails(postgres);
            database.update("UPDATE SYS_TENANT SET CODE = 'acme' WHERE ID = 11");

            database.update("""
                    INSERT INTO SYS_TENANT(ID, DELETED, CODE, SCHEMA_NAME)
                    VALUES (12, 1, 'acme', 'tenant_12')
                    """);
            database.execute("CREATE SCHEMA tenant_12");
            assertMigrationFails(postgres);
            database.update("DELETE FROM SYS_TENANT WHERE ID = 12");
            database.execute("DROP SCHEMA tenant_12");

            database.update("UPDATE SYS_TENANT SET SCHEMA_NAME = NULL WHERE ID = 11");
            assertMigrationFails(postgres);
            database.update("UPDATE SYS_TENANT SET SCHEMA_NAME = 'tenant_11' WHERE ID = 11");

            database.execute("ALTER SCHEMA tenant_11 RENAME TO orphan_11");
            assertMigrationFails(postgres);
            database.execute("ALTER SCHEMA orphan_11 RENAME TO tenant_11");

            database.execute("ALTER SCHEMA tenant_11 RENAME TO client_11");
            database.update("UPDATE SYS_TENANT SET SCHEMA_NAME = 'client_11' WHERE ID = 11");
            assertMigrationFails(postgres);
            database.execute("ALTER SCHEMA client_11 RENAME TO tenant_11");
            database.update("UPDATE SYS_TENANT SET SCHEMA_NAME = 'tenant_11' WHERE ID = 11");

            database.execute("CREATE SCHEMA tenant_acme");
            assertMigrationFails(postgres);

            assertTrue(schemaExists(database, "tenant_11"));
            assertTrue(schemaExists(database, "tenant_acme"));
            assertEquals("tenant_11", database.queryForObject(
                    "SELECT SCHEMA_NAME FROM SYS_TENANT WHERE ID = 11", String.class));
            assertEquals("safe", database.queryForObject(
                    "SELECT value FROM tenant_11.sentinel", String.class));
        }
    }

    private void createTenantTable(JdbcTemplate database) {
        database.execute("""
                CREATE TABLE SYS_TENANT (
                    ID BIGINT PRIMARY KEY,
                    DELETED BIGINT NOT NULL DEFAULT 0,
                    CODE VARCHAR(100),
                    SCHEMA_NAME VARCHAR(63)
                )
                """);
        database.execute("CREATE UNIQUE INDEX UK_SYS_TENANT_CODE ON SYS_TENANT(CODE) WHERE DELETED = 0");
        database.execute("""
                CREATE UNIQUE INDEX UK_SYS_TENANT_SCHEMA_NAME ON SYS_TENANT(SCHEMA_NAME)
                WHERE SCHEMA_NAME IS NOT NULL AND DELETED = 0
                """);
    }

    private void assertMigrationFails(EmbeddedPostgres postgres) throws Exception {
        assertThrows(Exception.class, () -> migrateInTransaction(postgres, MIGRATION));
    }

    private void migrateInTransaction(EmbeddedPostgres postgres, String path) throws Exception {
        String script = StreamUtils.copyToString(
                new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8);
        try (Connection connection = postgres.getPostgresDatabase().getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute(script);
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private boolean schemaExists(JdbcTemplate database, String schema) {
        return Boolean.TRUE.equals(database.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = ?)", Boolean.class, schema));
    }
}
