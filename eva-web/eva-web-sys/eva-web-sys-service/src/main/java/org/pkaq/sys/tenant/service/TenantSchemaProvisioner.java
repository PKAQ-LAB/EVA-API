package org.pkaq.sys.tenant.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.mybatis.tenant.TrustedTenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 由平台控制面创建可信租户 schema 并执行租户侧迁移。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class TenantSchemaProvisioner {
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final EvaConfig evaConfig;
    private final TrustedTenantSchemaResolver resolver;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void provision(Long tenantId) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return;
        }
        if (tenantId == null || tenantId <= 0L) {
            throw new IllegalArgumentException("租户 ID 非法");
        }
        String schema = evaConfig.getTenant().getPrefix() + tenantId;
        resolver.validate(schema);
        jdbcTemplate.queryForObject("SELECT EVA_PROVISION_TENANT_SCHEMA(?)", Object.class, schema);
        int updated = jdbcTemplate.update("""
                UPDATE SYS_TENANT SET SCHEMA_NAME = ?
                WHERE ID = ? AND COALESCE(DELETED, 0) = 0 AND COALESCE(FROZEN, 0) <> 1
                """, schema, tenantId);
        if (1 != updated) {
            throw new IllegalStateException("租户不存在或已禁用");
        }
        executeTenantMigrations(schema);
    }

    private void executeTenantMigrations(String schema) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        String originalPath = null;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT pg_catalog.set_config('search_path', ?, true)")) {
            try (PreparedStatement query = connection.prepareStatement("SELECT current_setting('search_path')");
                 ResultSet result = query.executeQuery()) {
                result.next();
                originalPath = result.getString(1);
            }
            statement.setString(1, schema);
            statement.execute();
            // 已完成账号迁移的 schema 不得再次执行 V1 重新生成旧用户表。
            if (!hasMigration(connection, "1")) {
                ScriptUtils.executeSqlScript(connection,
                        new ClassPathResource("db/tenant-migration/V1__TENANT_SCHEMA_BASE.sql"));
            }
            if (!hasMigration(connection, "2")) {
                ScriptUtils.executeSqlScript(connection,
                        new EncodedResource(new ClassPathResource("db/tenant-migration/V2__ACCOUNT_PROFILE_SPLIT.sql")),
                        false, false, "--", ScriptUtils.EOF_STATEMENT_SEPARATOR, "/*", "*/");
            }
            statement.setString(1, originalPath);
            statement.execute();
        } catch (Exception exception) {
            throw new IllegalStateException("租户 schema 初始化失败", exception);
        } finally {
            // 异常会使 PostgreSQL 事务回滚，事务级 search_path 随之恢复。
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private boolean hasMigration(Connection connection, String version) throws Exception {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT to_regclass('eva_tenant_schema_version') IS NOT NULL");
             ResultSet result = query.executeQuery()) {
            result.next();
            if (!result.getBoolean(1)) {
                return false;
            }
        }
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT 1 FROM EVA_TENANT_SCHEMA_VERSION WHERE VERSION = ?")) {
            query.setString(1, version);
            try (ResultSet result = query.executeQuery()) {
                return result.next();
            }
        }
    }
}
