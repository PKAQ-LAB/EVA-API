package org.pkaq.core.auth.tenant;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.mybatis.tenant.TenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 从 core 控制面解析租户，禁止接收客户端 schema 名。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class TenantLoginResolver {
    private final JdbcTemplate jdbcTemplate;
    private final EvaConfig evaConfig;
    private final TenantSchemaResolver tenantSchemaResolver;

    public TenantLoginIdentity resolveCode(String tenantCode) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return new TenantLoginIdentity(0L, 0L);
        }
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new IllegalArgumentException("schema模式登录必须提供tenantCode");
        }
        Long tenantId = jdbcTemplate.queryForObject("""
                SELECT ID FROM SYS_TENANT
                WHERE CODE = ? AND COALESCE(DELETED, 0) = 0
                  AND COALESCE(FROZEN, 0) <> 1
                  AND (EXPIRATION_DATE IS NULL OR EXPIRATION_DATE > CURRENT_TIMESTAMP)
                """, Long.class, tenantCode.trim());
        return resolveId(tenantId);
    }

    public TenantLoginIdentity resolveId(Long tenantId) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return new TenantLoginIdentity(0L, 0L);
        }
        if (tenantId == null || tenantId <= 0L) {
            throw new IllegalArgumentException("缺少可信租户ID");
        }
        Integer available = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM SYS_TENANT
                WHERE ID = ? AND COALESCE(DELETED, 0) = 0
                  AND COALESCE(FROZEN, 0) <> 1
                  AND (EXPIRATION_DATE IS NULL OR EXPIRATION_DATE > CURRENT_TIMESTAMP)
                """, Integer.class, tenantId);
        if (available == null || available != 1) {
            throw new IllegalStateException("租户不可用");
        }
        String schema = tenantSchemaResolver.resolve(tenantId);
        return new TenantLoginIdentity(tenantId, generation(schema));
    }

    private long generation(String schema) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(schema.getBytes(StandardCharsets.UTF_8));
            return Long.parseUnsignedLong(HexFormat.of().formatHex(digest, 0, 8), 16);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("无法计算租户schema代次", exception);
        }
    }
}
