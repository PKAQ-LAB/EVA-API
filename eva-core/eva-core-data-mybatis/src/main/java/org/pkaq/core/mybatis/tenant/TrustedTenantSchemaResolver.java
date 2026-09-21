package org.pkaq.core.mybatis.tenant;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 仅从平台控制面租户记录解析 schema，禁止客户端提供 schema 名。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class TrustedTenantSchemaResolver implements TenantSchemaResolver {
    private static final Pattern SAFE_SCHEMA = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");

    private final JdbcTemplate jdbcTemplate;
    private final EvaConfig evaConfig;

    @Override
    public String resolve(Long tenantId) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return evaConfig.getTenant().getStandaloneSchema();
        }
        if (tenantId == null || tenantId <= 0L) {
            throw new IllegalStateException("缺少可信租户上下文");
        }
        String schema = jdbcTemplate.queryForObject("""
                SELECT SCHEMA_NAME FROM SYS_TENANT
                WHERE ID = ? AND COALESCE(DELETED, 0) = 0 AND COALESCE(FROZEN, 0) <> 1
                """, String.class, tenantId);
        validate(schema);
        return schema;
    }

    public void validate(String schema) {
        String prefix = evaConfig.getTenant().getPrefix();
        if (schema == null || !SAFE_SCHEMA.matcher(schema).matches()
                || prefix == null || !schema.startsWith(prefix)) {
            throw new IllegalStateException("租户 schema 配置非法");
        }
    }

}
