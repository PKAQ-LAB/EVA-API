package org.pkaq.sys.tenant.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.mybatis.tenant.TrustedTenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.tenant.util.TenantCodeRules;
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
import java.util.List;
import java.util.regex.Pattern;

/**
 * 由平台控制面创建可信租户 schema 并执行租户侧迁移。
 *
 * @author PKAQ
 * @date 2026-10-09
 */
@Service
@RequiredArgsConstructor
public class TenantSchemaProvisioner {
    private static final Pattern SAFE_SCHEMA = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final EvaConfig evaConfig;
    private final TrustedTenantSchemaResolver resolver;

    /**
     * 使用平台数据库的编码和映射创建或幂等初始化租户 Schema，不接受客户端名称。
     *
     * @param tenantId 租户主键
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void provision(Long tenantId) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return;
        }
        if (tenantId == null || tenantId <= 0L) {
            throw new IllegalArgumentException("租户 ID 非法");
        }
        String core = evaConfig.getTenant().getCoreSchema();
        if (null == core || !SAFE_SCHEMA.matcher(core).matches()) {
            throw new IllegalStateException("平台 Schema 配置非法");
        }
        String coreTable = "\"" + core + "\".SYS_TENANT";
        List<TenantMetadata> tenants = jdbcTemplate.query(
                "SELECT CODE, SCHEMA_NAME FROM " + coreTable
                        + " WHERE ID = ? AND COALESCE(DELETED, 0) = 0 AND COALESCE(FROZEN, 0) <> 1 FOR UPDATE",
                (result, rowNumber) -> new TenantMetadata(result.getString("CODE"), result.getString("SCHEMA_NAME")),
                tenantId);
        if (1 != tenants.size()) {
            throw new IllegalStateException("租户不存在或已禁用");
        }
        TenantMetadata tenant = tenants.getFirst();
        if (!TenantCodeRules.isValid(tenant.code())) {
            SysCodes.TENANT_CODE_INVALID.newException();
        }
        Long codeOwners = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + coreTable + " WHERE CODE = ? AND ID <> ?",
                Long.class, tenant.code(), tenantId);
        if (null == codeOwners || 0L != codeOwners) {
            SysCodes.TENANT_CODE_ALREADY_EXIST.newException();
        }
        String prefix = evaConfig.getTenant().getPrefix();
        if (null == prefix || !SAFE_SCHEMA.matcher(prefix).matches()) {
            throw new IllegalStateException("租户 Schema 前缀配置非法");
        }
        // 已保存的映射始终权威，重复初始化不得凭 code 推导后改绑到另一套数据。
        String schema = null == tenant.schema() ? prefix + tenant.code() : tenant.schema();
        resolver.validate(schema);
        Long otherOwners = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + coreTable + " WHERE SCHEMA_NAME = ? AND ID <> ?",
                Long.class, schema, tenantId);
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_catalog.pg_namespace WHERE nspname = ?)", Boolean.class, schema);
        if (null == otherOwners || 0L != otherOwners || null == exists
                || (Boolean.TRUE.equals(exists) && !schema.equals(tenant.schema()))) {
            SysCodes.TENANT_SCHEMA_OCCUPIED.newException();
        }
        // 已绑定的 Schema 消失意味着可能丢失数据，禁止自动重建空库掩盖问题。
        if (null != tenant.schema() && !Boolean.TRUE.equals(exists)) {
            SysCodes.TENANT_SCHEMA_OCCUPIED.newException();
        }
        if (!exists) {
            // 数据库函数同样必须严格创建，禁止 IF NOT EXISTS 跨租户复用并发出现的 Schema。
            jdbcTemplate.queryForObject("SELECT \"" + core + "\".EVA_PROVISION_TENANT_SCHEMA(?)", Object.class, schema);
        }
        if (null == tenant.schema()) {
            int updated = jdbcTemplate.update("UPDATE " + coreTable + " SET SCHEMA_NAME = ?"
                    + " WHERE ID = ? AND SCHEMA_NAME IS NULL AND COALESCE(DELETED, 0) = 0"
                    + " AND COALESCE(FROZEN, 0) <> 1", schema, tenantId);
            if (1 != updated) {
                throw new IllegalStateException("租户 Schema 映射更新失败");
            }
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

    /** 平台记录提供的初始化元数据，不包含客户端输入。 */
    private record TenantMetadata(String code, String schema) {
    }
}
