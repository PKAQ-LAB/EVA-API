package org.pkaq.sys.tenant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mybatis.tenant.TrustedTenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.sys.SysCodes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 验证初始化使用数据库 code、权威映射和严格归属，不访问数据库。
 *
 * @author PKAQ
 * @date 2026-10-09
 */
class TenantSchemaProvisionerIdentityTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final DataSource dataSource = mock(DataSource.class);
    private final EvaConfig config = new EvaConfig();
    private TenantSchemaProvisioner provisioner;

    /** 真实名称校验与模拟 JDBC 配合，保留 SQL 参数证据。 */
    @BeforeEach
    void setUp() {
        config.getTenant().setEnable(true);
        config.getTenant().setMode(TenantProperties.MODE_SCHEMA);
        provisioner = new TenantSchemaProvisioner(jdbc, dataSource, config,
                new TrustedTenantSchemaResolver(jdbc, config));
    }

    /** 未绑定名称时必须来自数据库 code，而非数字主键。 */
    @Test
    void derivesSchemaFromDatabaseCode() throws Exception {
        metadata("acme", null);
        ownership("acme", "tenant_acme", false, 0L);
        when(jdbc.update(contains("SET SCHEMA_NAME"), eq("tenant_acme"), eq(101L))).thenReturn(1);
        completedMigrations();

        provisioner.provision(101L);

        verify(jdbc).queryForObject("SELECT \"eva_core\".EVA_PROVISION_TENANT_SCHEMA(?)",
                Object.class, "tenant_acme");
        verify(jdbc, never()).queryForObject(anyString(), eq(Object.class), eq("tenant_101"));
    }

    /** 仅同一租户已绑定的现有 Schema 可幂等初始化，不能改绑数字旧映射。 */
    @Test
    void preservesAuthoritativeExistingMapping() throws Exception {
        metadata("acme", "tenant_101");
        ownership("acme", "tenant_101", true, 0L);
        completedMigrations();

        provisioner.provision(101L);

        verify(jdbc, never()).queryForObject(anyString(), eq(Object.class), anyString());
        verify(jdbc, never()).update(anyString(), any(), any());
    }

    /** 已绑定但消失的 Schema 必须报错，不能新建空库伪装恢复成功。 */
    @Test
    void mappedMissingSchemaRejects() throws Exception {
        metadata("acme", "tenant_acme");
        ownership("acme", "tenant_acme", false, 0L);

        BizException failure = assertThrows(BizException.class, () -> provisioner.provision(101L));

        assertEquals(SysCodes.TENANT_SCHEMA_OCCUPIED, failure.getBizCode());
        verifyNoInteractions(dataSource);
        verify(jdbc, never()).queryForObject(anyString(), eq(Object.class), anyString());
        verify(jdbc, never()).update(anyString(), any(), any());
    }

    /** 现有 Schema 没有明示当前租户归属时禁止占用。 */
    @Test
    void rejectsExistingUnboundSchema() throws Exception {
        metadata("acme", null);
        ownership("acme", "tenant_acme", true, 0L);

        BizException failure = assertThrows(BizException.class, () -> provisioner.provision(101L));

        assertEquals(SysCodes.TENANT_SCHEMA_OCCUPIED, failure.getBizCode());
        verifyNoInteractions(dataSource);
        verify(jdbc, never()).queryForObject(anyString(), eq(Object.class), anyString());
    }

    /** 已被其他租户（含软删除租户）映射的名称不能复用。 */
    @Test
    void rejectsSchemaOwnedByAnotherTenant() throws Exception {
        metadata("acme", "tenant_acme");
        ownership("acme", "tenant_acme", true, 1L);

        assertThrows(BizException.class, () -> provisioner.provision(101L));
        verifyNoInteractions(dataSource);
    }

    /** 历史编码即便绕过业务服务也不能初始化到旧数据范围。 */
    @Test
    void rejectsCodeOwnedByHistoricalTenant() throws Exception {
        metadata("acme", null);
        when(jdbc.queryForObject(contains("WHERE CODE = ?"), eq(Long.class), eq("acme"), eq(101L)))
                .thenReturn(1L);

        BizException failure = assertThrows(BizException.class, () -> provisioner.provision(101L));

        assertEquals(SysCodes.TENANT_CODE_ALREADY_EXIST, failure.getBizCode());
        verifyNoInteractions(dataSource);
    }

    /** 过长最终名称在创建前被拒绝，不能依赖 PostgreSQL 自动截断。 */
    @Test
    void rejectsIdentifierExceedingPostgresLimit() throws Exception {
        config.getTenant().setPrefix("a".repeat(63));
        metadata("acme", null);
        when(jdbc.queryForObject(contains("WHERE CODE = ?"), eq(Long.class), eq("acme"), eq(101L)))
                .thenReturn(0L);

        assertThrows(IllegalStateException.class, () -> provisioner.provision(101L));
        verifyNoInteractions(dataSource);
    }

    private void metadata(String code, String schema) throws Exception {
        ResultSet row = mock(ResultSet.class);
        when(row.getString("CODE")).thenReturn(code);
        when(row.getString("SCHEMA_NAME")).thenReturn(schema);
        when(jdbc.query(contains("FOR UPDATE"), any(RowMapper.class), eq(101L)))
                .thenAnswer(invocation -> List.of(((RowMapper<?>) invocation.getArgument(1)).mapRow(row, 0)));
    }

    private void ownership(String code, String schema, boolean exists, Long otherOwners) {
        when(jdbc.queryForObject(contains("WHERE CODE = ?"), eq(Long.class), eq(code), eq(101L)))
                .thenReturn(0L);
        when(jdbc.queryForObject(contains("WHERE SCHEMA_NAME = ?"), eq(Long.class), eq(schema), eq(101L)))
                .thenReturn(otherOwners);
        when(jdbc.queryForObject(contains("pg_catalog.pg_namespace"), eq(Boolean.class), eq(schema)))
                .thenReturn(exists);
    }

    private void completedMigrations() throws Exception {
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        PreparedStatement setPath = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT pg_catalog.set_config('search_path', ?, true)"))
                .thenReturn(setPath);
        PreparedStatement readPath = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT current_setting('search_path')")).thenReturn(readPath);
        ResultSet path = mock(ResultSet.class);
        when(readPath.executeQuery()).thenReturn(path);
        when(path.getString(1)).thenReturn("eva_core");
        PreparedStatement findTable = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT to_regclass('eva_tenant_schema_version') IS NOT NULL"))
                .thenReturn(findTable);
        ResultSet table = mock(ResultSet.class);
        when(findTable.executeQuery()).thenReturn(table);
        when(table.getBoolean(1)).thenReturn(true);
        PreparedStatement findVersion = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT 1 FROM EVA_TENANT_SCHEMA_VERSION WHERE VERSION = ?"))
                .thenReturn(findVersion);
        ResultSet version = mock(ResultSet.class);
        when(findVersion.executeQuery()).thenReturn(version);
        when(version.next()).thenReturn(true);
    }
}
