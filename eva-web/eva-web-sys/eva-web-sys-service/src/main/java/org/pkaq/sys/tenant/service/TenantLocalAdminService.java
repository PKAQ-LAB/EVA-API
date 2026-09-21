package org.pkaq.sys.tenant.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TargetTenantExecutor;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 在租户私有 schema 中维护系统管理员角色及有效资源副本。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class TenantLocalAdminService {
    private static final String ADMIN_ROLE_CODE = "TENANT_ADMIN";

    private final JdbcTemplate jdbcTemplate;
    private final EvaConfig evaConfig;
    private final TargetTenantExecutor targetTenantExecutor;
    private final CoreSchemaExecutor coreSchemaExecutor;

    public void synchronize(Long tenantId, Long adminId) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return;
        }
        targetTenantExecutor.execute(tenantId, () -> {
            Long roleId = jdbcTemplate.query("""
                    SELECT ID FROM SYS_ROLE
                    WHERE CODE = ? AND COALESCE(DELETED, 0) = 0
                    """, resultSet -> resultSet.next() ? resultSet.getLong(1) : null, ADMIN_ROLE_CODE);
            if (roleId == null) {
                roleId = IdWorker.getId();
                jdbcTemplate.update("""
                        INSERT INTO SYS_ROLE(ID, REVISION, DELETED, FROZEN, SORT, CODE, NAME, DATA_SCOPE)
                        VALUES (?, 0, 0, 9999, 0, ?, '租户管理员', '0000')
                        """, roleId, ADMIN_ROLE_CODE);
            }
            jdbcTemplate.update("""
                    INSERT INTO SYS_ROLEUSER_REF(ROLE_ID, USER_ID) VALUES (?, ?)
                    ON CONFLICT (ROLE_ID, USER_ID) DO NOTHING
                    """, roleId, adminId);

            List<Long> resources = coreSchemaExecutor.execute(template -> template.queryForList("""
                    SELECT RESOURCE_ID FROM SYS_TENANT_RESOURCE WHERE TENANT_ID = ?
                    """, Long.class, tenantId));
            jdbcTemplate.update("DELETE FROM SYS_ROLERES_REF WHERE ROLE_ID = ?", roleId);
            for (Long resourceId : resources) {
                jdbcTemplate.update("""
                        INSERT INTO SYS_ROLERES_REF(ROLE_ID, RESOURCE_ID) VALUES (?, ?)
                        ON CONFLICT (ROLE_ID, RESOURCE_ID) DO NOTHING
                        """, roleId, resourceId);
            }
            return null;
        });
    }
}
