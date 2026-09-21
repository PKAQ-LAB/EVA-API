package org.pkaq.sys.tenant.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.mybatis.tenant.TrustedTenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

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
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT pg_catalog.set_config('search_path', ?, true)")) {
            statement.setString(1, schema);
            statement.execute();
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/tenant-migration/V1__TENANT_SCHEMA_BASE.sql"));
        } catch (Exception exception) {
            throw new IllegalStateException("租户 schema 初始化失败", exception);
        }
    }
}
